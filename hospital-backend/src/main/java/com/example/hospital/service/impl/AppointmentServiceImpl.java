package com.example.hospital.service.impl;

import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.dto.MedicalRecordDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.Schedule;
import com.example.hospital.entity.Visit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.mapper.VisitMapper;
import com.example.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private VisitMapper visitMapper;

    @Override
    public List<Appointment> getTodayAppointmentsByDoctorId(String doctorId, LocalDate today) {
        List<Appointment> appointments = appointmentMapper.selectTodayAppointmentsByDoctorId(doctorId, today);

        // 为每个appointment添加patient信息
        for (Appointment appointment : appointments) {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient != null) {
                // 动态添加patient信息到appointment对象
                appointment.setPatientName(patient.getPatientName());
                appointment.setPatientGender(patient.getPatientGender());
                appointment.setPatientBirthday(patient.getPatientBirthday());
            }
        }

        return appointments;
    }

    @Override
    public List<AppointmentDetailDTO> getAppointmentWithDetails(String appointmentId) {
        List<AppointmentDetailDTO> appointments = appointmentMapper.selectAppointmentWithDetails(appointmentId);

        // 为每个appointment添加patient信息
        for (AppointmentDetailDTO appointment : appointments) {
            if (appointment != null) {
                // 单独查询patient信息
                Patient patient = patientMapper.selectById(appointment.getPatientId());
                if (patient != null) {
                    appointment.setPatientName(patient.getPatientName());
                    appointment.setPatientGender(patient.getPatientGender());
                    appointment.setPatientBirthday(patient.getPatientBirthday());
                    appointment.setPatientPhone(patient.getPatientPhone());
                }
            }
        }
        return appointments;
    }

    @Override
    public Appointment updateAppointmentStatus(String appointmentId, String status) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        if (appointments.size() == 1) {
            Appointment appointment = appointments.get(0);
            appointment.setStatus(status);
            appointmentMapper.updateById(appointment);
            return appointment;
        }
        return null;
    }

    @Override
    public boolean startConsultation(String appointmentId, String visitTimeStr) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        Appointment appointment = null;

        if (appointments.size() == 1) {
            appointment = appointments.get(0);
        } else if (appointments.size() > 1) {
            // 如果有多条记录，选择状态为BOOKED的
            for (Appointment app : appointments) {
                if ("BOOKED".equals(app.getStatus())) {
                    appointment = app;
                    break;
                }
            }
        }

        if (appointment != null && "BOOKED".equals(appointment.getStatus())) {
            // 检查是否已存在visit记录
            List<Visit> existingVisits = visitMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit>()
                    .eq("appointment_id", appointmentId)
            );

            if (existingVisits.isEmpty()) {
                // 解析前端传递的本地时间字符串
                LocalDateTime visitTime = parseLocalDateTimeString(visitTimeStr);

                // 创建就诊记录
                Visit visit = new Visit();
                visit.setVisitId(UUID.randomUUID().toString().replace("-", ""));
                visit.setAppointmentId(appointmentId);
                visit.setHospitalId(appointment.getHospitalId());
                visit.setVisitTime(visitTime); // 设置为排班的工作日期时间
                visit.setDiagnosis(""); // 初始诊断为空

                visitMapper.insert(visit);
            }
            // 如果已存在visit记录，保持不变（可能是重新开始接诊）

            return true;
        }
        return false;
    }

    @Override
    public boolean completeConsultation(String appointmentId, String patientId, String diagnosis) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        Appointment appointment = null;
        if (appointments.size() == 1) {
            appointment = appointments.get(0);
        } else if (appointments.size() > 1) {
            // 如果有多条记录，根据patientId选择正确的记录
            for (Appointment app : appointments) {
                if (patientId.equals(app.getPatientId())) {
                    appointment = app;
                    break;
                }
            }
            // 如果没找到匹配的patientId，回退到原来的逻辑
            if (appointment == null) {
                for (Appointment app : appointments) {
                    if ("BOOKED".equals(app.getStatus())) {
                        appointment = app;
                        break;
                    }
                }
            }
        }

        if (appointment != null && ("BOOKED".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus()))) {
            // 如果是BOOKED状态，先更新为COMPLETED
            if ("BOOKED".equals(appointment.getStatus())) {
                appointmentMapper.updateAppointmentStatus("COMPLETED", appointmentId);
            }

            // 更新visit记录的诊断结果（visit记录应该在startConsultation时已创建）
            List<Visit> existingVisits = visitMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit>()
                    .eq("appointment_id", appointmentId)
            );

            if (!existingVisits.isEmpty()) {
                // 更新现有记录，只更新诊断结果，保持visit_time不变
                Visit existingVisit = existingVisits.get(0);
                visitMapper.updateDiagnosis(diagnosis, existingVisit.getVisitId());
            } else {
                // 如果没有visit记录，说明startConsultation没有正常执行，这里作为兜底处理
                Visit visit = new Visit();
                visit.setVisitId(UUID.randomUUID().toString().replace("-", ""));
                visit.setAppointmentId(appointmentId);
                visit.setHospitalId(appointment.getHospitalId());
                visit.setVisitTime(LocalDateTime.now()); // 兜底情况下设置为当前时间（使用系统默认时区）
                visit.setDiagnosis(diagnosis);

                visitMapper.insert(visit);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getDiagnosis(String appointmentId) {
        // 从visit表中查询诊断结果
        List<com.example.hospital.entity.Visit> visits = visitMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.hospital.entity.Visit>()
                .eq("appointment_id", appointmentId)
                .orderByDesc("visit_time")
                .last("LIMIT 1")
        );

        if (!visits.isEmpty()) {
            return visits.get(0).getDiagnosis();
        }
        return null;
    }

    /**
     * 解析前端传递的本地时间字符串
     * 前端格式：2025/12/25 23:54:26
     * @param timeStr 时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseLocalDateTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            // 前端格式：2025/12/25 23:54:26，需要转换为标准格式
            String standardFormat = timeStr.replace("/", "-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(standardFormat, formatter);
        } catch (Exception e) {
            log.error("解析时间字符串失败: {}", timeStr, e);
            return LocalDateTime.now();
        }
    }

    @Override
    public Page<MedicalRecordDTO> getMedicalRecords(String userId, String month, int page, int pageSize) {
        Page<MedicalRecordDTO> pageResult = new Page<>(page, pageSize);

        // 首先通过userId找到对应的doctorId
        Doctor doctor = doctorMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Doctor>()
                .eq("user_id", userId)
        );

        if (doctor == null) {
            pageResult.setRecords(new java.util.ArrayList<>());
            pageResult.setTotal(0);
            return pageResult;
        }

        String doctorId = doctor.getDoctorId();

        // 然后找到该医生的所有预约ID，避免复杂的跨表查询
        List<String> appointmentIds = appointmentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Appointment>()
                .inSql("schedule_id", "SELECT schedule_id FROM schedule WHERE doctor_id = '" + doctorId + "'")
                .select("appointment_id")
        ).stream().map(Appointment::getAppointmentId).collect(java.util.stream.Collectors.toList());

        if (appointmentIds.isEmpty()) {
            pageResult.setRecords(new java.util.ArrayList<>());
            pageResult.setTotal(0);
            return pageResult;
        }

        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.in("appointment_id", appointmentIds);

        // 月份筛选
        if (month != null && !month.trim().isEmpty()) {
            queryWrapper.apply("DATE_FORMAT(visit_time, '%Y-%m') = '" + month + "'");
        }

        // 分页查询visit记录
        Page<Visit> visitPage = visitMapper.selectPage(new Page<>(page, pageSize), queryWrapper);

        // 转换为DTO
        Page<MedicalRecordDTO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(visitPage.getTotal());

        java.util.List<MedicalRecordDTO> records = new java.util.ArrayList<>();
        for (Visit visit : visitPage.getRecords()) {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setRecordId(visit.getVisitId());
            dto.setVisitDate(visit.getVisitTime().toLocalDate());
            dto.setSummary(visit.getDiagnosis());

            // 从appointment表获取更多信息
            Appointment appointment = appointmentMapper.selectBasicById(visit.getAppointmentId());
            if (appointment != null) {
                dto.setOrderNo(appointment.getAppointmentId());
                dto.setStatus(appointment.getStatus());

                // 获取患者信息
                Patient patient = patientMapper.selectById(appointment.getPatientId());
                if (patient != null) {
                    dto.setPatientName(patient.getPatientName());
                    dto.setPatientPhone(patient.getPatientPhone());
                }

                // 获取排班信息（时间段）
                Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                if (schedule != null) {
                    dto.setTimeSlot(schedule.getTimeSlot());

                    // 获取医生信息（科室）
                    Doctor scheduleDoctor = doctorMapper.selectById(schedule.getDoctorId());
                    if (scheduleDoctor != null && scheduleDoctor.getDepartmentId() != null) {
                        // 通过departmentId获取部门名称
                        // 这里需要从department表查询，或者通过现有的逻辑获取
                        dto.setDepartmentName("心血管内科"); // 临时设置，后面可以优化
                    }
                }
            }

            records.add(dto);
        }

        resultPage.setRecords(records);
        return resultPage;
    }
}

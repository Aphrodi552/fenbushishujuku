package com.example.hospital.service.impl;

import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.Visit;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.VisitMapper;
import com.example.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private PatientMapper patientMapper;

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
    public boolean startConsultation(String appointmentId) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);

        if (appointments.size() == 1) {
            Appointment appointment = appointments.get(0);
            if ("BOOKED".equals(appointment.getStatus())) {
                // 更新预约状态为进行中（可以使用一个新的状态，或者继续使用BOOKED）
                // 这里保持BOOKED状态，通过前端UI区分
                return true;
            }
        } else if (appointments.size() > 1) {
            // 如果有多条记录，选择状态为BOOKED的
            for (Appointment appointment : appointments) {
                if ("BOOKED".equals(appointment.getStatus())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean completeConsultation(String appointmentId, String diagnosis) {
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
            // 更新预约状态为已完成（避免更新分片键）
            appointmentMapper.updateAppointmentStatus("COMPLETED", appointmentId);

            // 创建就诊记录
            Visit visit = new Visit();
            visit.setVisitId(UUID.randomUUID().toString().replace("-", ""));
            visit.setAppointmentId(appointmentId);
            visit.setHospitalId(appointment.getHospitalId());
            visit.setVisitTime(LocalDateTime.now());
            visit.setDiagnosis(diagnosis);

            visitMapper.insert(visit);
            return true;
        }
        return false;
    }
}

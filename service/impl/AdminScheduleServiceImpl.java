package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.service.AdminScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminScheduleServiceImpl implements AdminScheduleService {




    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;

    public AdminScheduleServiceImpl(
            ScheduleMapper scheduleMapper,
            DoctorMapper doctorMapper,
            DepartmentMapper departmentMapper,
            HospitalMapper hospitalMapper
    ) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.hospitalMapper = hospitalMapper;
    }

    @Override
    public IPage<AdminScheduleVO> pageSchedules(AdminScheduleQueryRequest req) {
        if (!StringUtils.hasText(req.getHospitalId())) {
            throw new RuntimeException("hospitalId 必填（排班按院区分片）");
        }

        Page<Schedule> page = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
        // 分片路由必须带 hospitalId（schedule 的分片键）:contentReference[oaicite:5]{index=5}
        qw.eq(Schedule::getHospitalId, req.getHospitalId());

        if (StringUtils.hasText(req.getDoctorId())) {
            qw.eq(Schedule::getDoctorId, req.getDoctorId());
        }
        if (StringUtils.hasText(req.getTimeSlot())) {
            qw.eq(Schedule::getTimeSlot, req.getTimeSlot());
        }
        if (StringUtils.hasText(req.getWorkDateFrom())) {
            qw.ge(Schedule::getWorkDate, LocalDate.parse(req.getWorkDateFrom()));
        }
        if (StringUtils.hasText(req.getWorkDateTo())) {
            qw.le(Schedule::getWorkDate, LocalDate.parse(req.getWorkDateTo()));
        }
        qw.orderByDesc(Schedule::getWorkDate).orderByAsc(Schedule::getTimeSlot);

        IPage<Schedule> entityPage = scheduleMapper.selectPage(page, qw);

        // 批量补齐 doctor / department / hospital 名称（避免跨分片 join）
        String hospitalId = req.getHospitalId();
        Hospital hospital = hospitalMapper.selectById(hospitalId);

        List<String> doctorIds = entityPage.getRecords().stream()
                .map(Schedule::getDoctorId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Doctor> doctorMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<Doctor> docs = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getHospitalId, hospitalId)
                    .in(Doctor::getDoctorId, doctorIds));
            doctorMap = docs.stream().collect(Collectors.toMap(Doctor::getDoctorId, d -> d));
        }

        // 科室过滤（如果传了 departmentId：在 doctorMap 基础上做筛）
        final Set<String> allowedDoctorIds = StringUtils.hasText(req.getDepartmentId())
                ? doctorMap.values().stream()
                .filter(d -> Objects.equals(req.getDepartmentId(), d.getDepartmentId()))
                .map(Doctor::getDoctorId)
                .collect(Collectors.toSet())
                : null;

        // department 批量
        Set<String> deptIds = doctorMap.values().stream()
                .map(Doctor::getDepartmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Department> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Department> deps = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                    .eq(Department::getHospitalId, hospitalId)
                    .in(Department::getDepartmentId, deptIds));
            deptMap = deps.stream().collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        }

        // 若按 departmentId 过滤，则在内存中过滤记录（避免复杂 SQL join）
        List<Schedule> filtered = entityPage.getRecords();
        if (allowedDoctorIds != null) {
            filtered = filtered.stream()
                    .filter(s -> allowedDoctorIds.contains(s.getDoctorId()))
                    .collect(Collectors.toList());
        }

        // 组装 VO Page（保留分页总数：如果你需要“严格按部门过滤后的分页总数”，要改成 join 查询；先按可运行/稳定优先）
        Page<AdminScheduleVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AdminScheduleVO> voRecords = new ArrayList<>();

        for (Schedule s : filtered) {
            AdminScheduleVO vo = new AdminScheduleVO();
            vo.setScheduleId(s.getScheduleId());
            vo.setHospitalId(s.getHospitalId());
            vo.setHospitalName(hospital == null ? null : hospital.getHospitalName());

            Doctor doc = doctorMap.get(s.getDoctorId());
            vo.setDoctorId(s.getDoctorId());
            vo.setDoctorName(doc == null ? null : doc.getDoctorName());

            String deptId = (doc == null ? null : doc.getDepartmentId());
            vo.setDepartmentId(deptId);
            Department dep = (deptId == null ? null : deptMap.get(deptId));
            vo.setDepartmentName(dep == null ? null : dep.getDepartmentName());

            vo.setWorkDate(s.getWorkDate() == null ? null : s.getWorkDate().toString());
            vo.setTimeSlot(s.getTimeSlot());
            vo.setTotalQuota(s.getTotalQuota());
            vo.setBookedCount(s.getBookedCount());
            int remain = (s.getTotalQuota() == null ? 0 : s.getTotalQuota()) - (s.getBookedCount() == null ? 0 : s.getBookedCount());
            vo.setRemainingQuota(Math.max(remain, 0));

            voRecords.add(vo);
        }

        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public void createSchedule(AdminScheduleCreateRequest req) {
        if (!StringUtils.hasText(req.getHospitalId())) throw new RuntimeException("hospitalId 必填");
        if (!StringUtils.hasText(req.getDoctorId())) throw new RuntimeException("doctorId 必填");
        if (!StringUtils.hasText(req.getWorkDate())) throw new RuntimeException("workDate 必填");
        if (!StringUtils.hasText(req.getTimeSlot())) throw new RuntimeException("timeSlot 必填");
        if (req.getTotalQuota() == null || req.getTotalQuota() < 0) throw new RuntimeException("totalQuota 必须为非负数");

        // 校验医生是否属于该院区（避免插入路由错误）
        Doctor doc = doctorMapper.selectById(req.getDoctorId());
        if (doc == null) throw new RuntimeException("医生不存在");
        if (!Objects.equals(req.getHospitalId(), doc.getHospitalId())) {
            throw new RuntimeException("医生不属于该院区，无法创建排班");
        }

        Schedule s = new Schedule();
        s.setScheduleId(String.valueOf(IdWorker.getId()));
        s.setHospitalId(req.getHospitalId()); // 分片键：允许 insert
        s.setDoctorId(req.getDoctorId());
        s.setWorkDate(LocalDate.parse(req.getWorkDate()));
        s.setTimeSlot(req.getTimeSlot());
        s.setTotalQuota(req.getTotalQuota());
        s.setBookedCount(0);

        scheduleMapper.insert(s);
    }

    @Override
    public void updateSchedule(String scheduleId, AdminScheduleUpdateRequest req) {
        if (!StringUtils.hasText(scheduleId)) throw new RuntimeException("scheduleId 必填");

        Schedule exist = scheduleMapper.selectById(scheduleId);
        if (exist == null) throw new RuntimeException("排班不存在");

        // 校验：不允许修改 hospitalId（分片键），因此更新只操作非分片字段
        if (!StringUtils.hasText(req.getDoctorId())) throw new RuntimeException("doctorId 必填");
        if (!StringUtils.hasText(req.getWorkDate())) throw new RuntimeException("workDate 必填");
        if (!StringUtils.hasText(req.getTimeSlot())) throw new RuntimeException("timeSlot 必填");
        if (req.getTotalQuota() == null || req.getTotalQuota() < 0) throw new RuntimeException("totalQuota 必须为非负数");

        // 若更换医生：必须仍属于同一个院区（否则等于想改分片路由）
        Doctor doc = doctorMapper.selectById(req.getDoctorId());
        if (doc == null) throw new RuntimeException("医生不存在");
        if (!Objects.equals(exist.getHospitalId(), doc.getHospitalId())) {
            throw new RuntimeException("不允许跨院区更换医生（会导致分片键变更）");
        }

        int booked = exist.getBookedCount() == null ? 0 : exist.getBookedCount();
        if (req.getTotalQuota() < booked) {
            throw new RuntimeException("总号源不能小于已预约数：" + booked);
        }

        // 关键：LambdaUpdateWrapper 的 set 不包含 hospitalId；where 带 hospitalId 做路由
        LambdaUpdateWrapper<Schedule> uw = new LambdaUpdateWrapper<>();
        uw.eq(Schedule::getScheduleId, scheduleId);
        uw.eq(Schedule::getHospitalId, exist.getHospitalId()); // 分片路由条件

        uw.set(Schedule::getDoctorId, req.getDoctorId());
        uw.set(Schedule::getWorkDate, LocalDate.parse(req.getWorkDate()));
        uw.set(Schedule::getTimeSlot, req.getTimeSlot());
        uw.set(Schedule::getTotalQuota, req.getTotalQuota());

        scheduleMapper.update(null, uw);
    }

    @Override
    public void deleteSchedule(String scheduleId) {
        if (!StringUtils.hasText(scheduleId)) throw new RuntimeException("scheduleId 必填");

        Schedule exist = scheduleMapper.selectById(scheduleId);
        if (exist == null) return;

        // deleteById 可能触发广播；这里带 hospitalId 精准路由
        LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
        qw.eq(Schedule::getScheduleId, scheduleId);
        qw.eq(Schedule::getHospitalId, exist.getHospitalId());
        scheduleMapper.delete(qw);
    }

    @Override
    public List<Hospital> listHospitals() {
        return hospitalMapper.selectList(null);
    }

    @Override
    public List<Department> listDepartments(String hospitalId) {
        if (!StringUtils.hasText(hospitalId)) throw new RuntimeException("hospitalId 必填");
        return departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, hospitalId));
    }

    @Override
    public List<Doctor> listDoctors(String hospitalId, String departmentId) {
        if (!StringUtils.hasText(hospitalId)) throw new RuntimeException("hospitalId 必填");

        LambdaQueryWrapper<Doctor> qw = new LambdaQueryWrapper<>();
        qw.eq(Doctor::getHospitalId, hospitalId);
        if (StringUtils.hasText(departmentId)) {
            qw.eq(Doctor::getDepartmentId, departmentId);
        }
        qw.orderByAsc(Doctor::getDoctorName);
        return doctorMapper.selectList(qw);
    }



}

package com.example.hospital.service.impl;

import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.service.DoctorService;
import com.example.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 排班服务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorService doctorService;

    @Override
    public List<Schedule> getSchedulesByDoctorId(String doctorId) {
        return scheduleMapper.selectByDoctorId(doctorId);
    }

    @Override
    public List<Schedule> getSchedulesByDoctorIdAndDateRange(String doctorId, LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectByDoctorIdAndDateRange(doctorId, startDate, endDate);
    }

    @Override
    public List<Schedule> getSchedulesByDoctorIdAndDateRangeAndTimeSlot(String doctorId, LocalDate startDate, LocalDate endDate, String timeSlot) {
        return scheduleMapper.selectByDoctorIdAndDateRangeAndTimeSlot(doctorId, startDate, endDate, timeSlot);
    }

    @Override
    public Schedule createSchedule(Schedule schedule) {
        // 生成UUID作为主键
        schedule.setScheduleId(UUID.randomUUID().toString().replace("-", ""));

        // 如果没有设置bookedCount，默认为0
        if (schedule.getBookedCount() == null) {
            schedule.setBookedCount(0);
        }

        scheduleMapper.insert(schedule);
        return schedule;
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) {
        scheduleMapper.updateById(schedule);
        return schedule;
    }

    @Override
    public void deleteSchedule(String scheduleId) {
        scheduleMapper.deleteById(scheduleId);
    }
}

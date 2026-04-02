package org.example.highservice.service.Impl;

import org.example.highservice.entity.SubjectCombination;
import org.example.highservice.mapper.SubjectCombinationMapper;
import org.example.highservice.service.SubjectCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SubjectCombinationServiceImpl implements SubjectCombinationService {

    @Autowired
    private SubjectCombinationMapper subjectCombinationMapper;

    @Override
    public List<Map<String, Object>> getHotCombinations() {
        return subjectCombinationMapper.getHotCombinations();
    }

    @Override
    public List<SubjectCombination> getCombinationsByFirstSubject(String firstSubject) {
        return subjectCombinationMapper.getCombinationsByFirstSubject(firstSubject);
    }

    @Override
    public List<Map<String, Object>> getAllCombinationsWithDetails() {
        return subjectCombinationMapper.getAllCombinationsWithDetails();
    }

    @Override
    public List<SubjectCombination> getCombinationsBySubject(Long subjectId) {
        return subjectCombinationMapper.getCombinationsBySubject(subjectId);
    }

    @Override
    public List<Map<String, Object>> getCombinationStudentCount() {
        return subjectCombinationMapper.getCombinationStudentCount();
    }
}
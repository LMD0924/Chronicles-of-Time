package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.Major;
import org.example.universityservice.vo.major.MajorCompareVO;

import java.util.List;

public interface MajorService extends IService<Major> {

    List<Major> getAllMajors();

    Major getMajorById(Long id);

    Major getMajorByCode(String code);

    boolean createMajor(Major major);

    boolean updateMajor(Major major);

    boolean deleteMajor(Long id);

    MajorCompareVO compareMajors(Long majorId1, Long majorId2);
}
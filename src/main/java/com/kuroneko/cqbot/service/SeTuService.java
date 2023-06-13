package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.vo.SeTuData;
import com.kuroneko.cqbot.vo.SeTuVo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SeTuService {
    private final RestTemplate restTemplate;

    public SeTuService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SeTuVo getSeTuVo(String tag) {
        ResponseEntity<SeTuVo> entity = restTemplate.getForEntity(Constant.SE_TU_URL, SeTuVo.class, tag);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        return entity.getBody();
    }

    public SeTuData getSeTuData(String tag) {
        SeTuVo seTuVo = getSeTuVo(tag);
        if (seTuVo == null) {
            return null;
        }
        List<SeTuData> data = seTuVo.getData();
        if (!data.isEmpty()) {
            return data.get(0);

        }
        return null;
    }
}

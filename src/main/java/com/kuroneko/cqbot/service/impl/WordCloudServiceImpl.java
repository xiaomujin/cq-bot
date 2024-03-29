package com.kuroneko.cqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.service.WordCloudService;
import com.kuroneko.cqbot.mapper.WordCloudMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【word_cloud(群友聊天记录)】的数据库操作Service实现
* @createDate 2024-03-29 17:05:44
*/
@Service
public class WordCloudServiceImpl extends ServiceImpl<WordCloudMapper, WordCloud>
    implements WordCloudService{

}





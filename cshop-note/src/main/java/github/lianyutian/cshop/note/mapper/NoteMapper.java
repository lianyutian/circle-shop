package github.lianyutian.cshop.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.note.model.po.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * NoteMapper
 *
 * @author lianyutian
 * @since 2024-12-24 14:19:30
 * @version 1.0
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {}

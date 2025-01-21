package github.lianyutian.cshop.note.controller;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.note.model.param.NoteAddParam;
import github.lianyutian.cshop.note.model.param.NoteEditParam;
import github.lianyutian.cshop.note.model.vo.NoteDetailVO;
import github.lianyutian.cshop.note.service.NoteService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NoteController
 *
 * @author lianyutian
 * @since 2025-01-02 17:14:17
 * @version 1.0
 */
@RestController
@RequestMapping("/api/note/v1")
@AllArgsConstructor
public class NoteController {

  private final NoteService noteService;

  /**
   * 添加笔记
   *
   * @param noteAddParam 笔记入参
   * @return 添加结果
   */
  @PostMapping("add")
  public ApiResult<Void> addNote(@RequestBody NoteAddParam noteAddParam) {
    int added = noteService.addNote(noteAddParam);
    return added > 0 ? ApiResult.success() : ApiResult.result(BizCodeEnum.NOTE_ADD_FAIL);
  }

  /**
   * 更新我的笔记
   *
   * @param noteAddParam 更新笔记入参
   * @return 更新结果
   */
  @PostMapping("edit")
  public ApiResult<Void> updateNote(@RequestBody NoteEditParam noteAddParam) {
    int updated = noteService.updateNote(noteAddParam);
    return updated > 0 ? ApiResult.success() : ApiResult.result(BizCodeEnum.NOTE_UPDATE_FAIL);
  }

  /**
   * 删除我的笔记
   *
   * @param noteId noteId
   * @return 删除结果
   */
  @GetMapping("delete/{noteId}")
  public ApiResult<Void> delete(@PathVariable("noteId") long noteId) {
    int deleted = noteService.deleteNote(noteId);
    return deleted > 0 ? ApiResult.success() : ApiResult.result(BizCodeEnum.NOTE_DEL_FAIL);
  }

  /**
   * 获取我的笔记详情
   *
   * @param noteId noteId
   * @return 笔记详情信息
   */
  @GetMapping("/detail/{noteId}")
  public ApiResult<NoteDetailVO> detail(@PathVariable("noteId") Long noteId) {
    return ApiResult.success(noteService.getNoteDetail(noteId));
  }
}

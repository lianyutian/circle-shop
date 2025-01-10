package github.lianyutian.cshop.note.controller;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.model.vo.PageVO;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.note.model.param.NotePageParam;
import github.lianyutian.cshop.note.model.vo.NoteShowVO;
import github.lianyutian.cshop.note.service.NoteShowService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户笔记展示
 *
 * @author lianyutian
 * @since 2025-01-09 10:12:54
 * @version 1.0
 */
@RestController
@RequestMapping("/api/note/v1")
@AllArgsConstructor
public class NoteShowController {

  private final NoteShowService noteShowService;

  /**
   * 获取笔记详情
   *
   * @param noteId 笔记ID
   * @return 笔记详情
   */
  @PostMapping("detail/show")
  public ApiResult<NoteShowVO> getNoteShow(@RequestBody Long noteId) {
    if (noteId == null) {
      return ApiResult.result(BizCodeEnum.COMMON_PARAM_ERROR);
    }
    NoteShowVO noteShowVO = noteShowService.getNoteShow(noteId);
    return noteShowVO == null
        ? ApiResult.result(BizCodeEnum.NOTE_NOT_EXITS)
        : ApiResult.success(noteShowVO);
  }

  /**
   * 分页查询用户笔记列表
   *
   * @param notePageParam 笔记列表入参
   * @return 我的笔记列表
   */
  @PostMapping("list/show")
  public ApiResult<PageVO<NoteShowVO>> getNoteShowList(@RequestBody NotePageParam notePageParam) {
    return ApiResult.success(noteShowService.getNoteShowList(notePageParam));
  }
}

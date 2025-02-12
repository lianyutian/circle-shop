package github.lianyutian.cshop.note.constant;

/**
 * 笔记缓存常量
 *
 * @author lianyutian
 * @since 2025-01-08 14:06:12
 * @version 1.0
 */
public class NoteCacheKeyConstant {
  /** 笔记信息更新锁前缀 */
  public static final String NOTE_UPDATE_LOCK_KEY_PREFIX = "cshop-note:update:lock:";

  /** 笔记展示信息缓存前缀 */
  public static final String NOTE_SHOW_KEY_PREFIX = "cshop-note:show:";

  /** 笔记信息缓存前缀 */
  public static final String NOTE_DETAIL_KEY_PREFIX = "cshop-note:detail:";

  /** 笔记信息分页缓存前缀 */
  public static final String NOTE_SHOW_PAGE_KEY_PREFIX = "cshop-note:show-page:";

  /** 用户笔记信息总数缓存前缀 */
  public static final String NOTE_TOTAL_KEY_PREFIX = "cshop-note:total:";
}

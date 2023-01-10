package web.browser.dragon.huawei.utils.other.database

class Record {
    var desktopMode: Boolean?
    var nightMode: Boolean? = null
    var iconColor: Long
    var title: String?
    var uRL: String?
    var time: Long
    val ordinal: Int
    var type //0 History, 1 Start site, 2 Bookmark
            : Int

    constructor() {
        title = null
        uRL = null
        time = 0L
        ordinal = -1
        type = -1
        desktopMode = null
        iconColor = 0L
    }

    constructor(
        title: String?,
        url: String?,
        time: Long,
        ordinal: Int,
        type: Int,
        DesktopMode: Boolean?,
        NightMode: Boolean?,
        iconColor: Long
    ) {
        this.title = title
        uRL = url
        this.time = time
        this.ordinal = ordinal
        this.type = type
        desktopMode = DesktopMode
        nightMode = NightMode
        this.iconColor = iconColor
    }
}

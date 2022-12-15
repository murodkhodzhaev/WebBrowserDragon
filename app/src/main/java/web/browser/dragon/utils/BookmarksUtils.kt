package web.browser.dragon.utils

import web.browser.dragon.R
import web.browser.dragon.model.Bookmark

fun getBookmarks(): ArrayList<Bookmark> {

    val arr = arrayListOf<Bookmark>()

    arr.add(Bookmark(
        1,
        "Facebook",
        "https://www.facebook.com/",
        null,
        null,
        R.drawable.ic_facebook
    ))

    arr.add(Bookmark(
        2,
        "YouTube",
        "https://www.youtube.com/",
        null,
        null,
        R.drawable.ic_youtube
    ))

    arr.add(Bookmark(
        3,
        "Twitter",
        "https://www.twitter.com/",
        null,
        null,
        R.drawable.ic_twitter
    ))

    arr.add(Bookmark(
        4,
        "VK",
        "https://www.vk.com/",
        null,
        null,
        R.drawable.ic_vk
    ))

    arr.add(Bookmark(
        5,
        "Odnoklassniki",
        "https://www.ok.ru/",
        null,
        null,
        R.drawable.ic_odnoklassniki
    ))

    arr.add(Bookmark(
        6,
        "Hot Coubs - The Biggest Video Meme Platform",
        "https://coub.com/",
        "https://coub-assets.akamaized.net/assets/og/coub_og_image-ac413e288cf569b3fec8bcce869961e530d0f70adef8f94fb47883590e4d57fa.png",
        null,
        null,
        true
    ))

    arr.add(Bookmark(
        7,
        "Amazon.com. Spend less. Smile more.",
        "https://www.amazon.com/",
        "http://g-ec2.images-amazon.com/images/G/01/social/api-share/amazon_logo_500500._V323939215_.png",
        null,
        null,
        true
    ))

    return arr
}
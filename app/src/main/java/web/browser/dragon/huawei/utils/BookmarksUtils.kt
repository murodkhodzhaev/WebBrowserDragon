@file:Suppress("DUPLICATE_LABEL_IN_WHEN")

package web.browser.dragon.huawei.utils

import android.os.Build
import android.os.LocaleList
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.Bookmark
import java.util.*

//private val countryName = Locale.getDefault().country




fun getBookmarks(): ArrayList<Bookmark> {
    val countryName: String = if (Build.VERSION.SDK_INT >= 24) {
        LocaleList.getDefault()[0].language
    } else {
        Locale.getDefault().language
    }



    val arr = arrayListOf<Bookmark>()

    arr.add(
        Bookmark(
            1,
            "Facebook",
            "https://www.facebook.com/",
            null,
            null,
            R.drawable.ic_facebook
        )
    )

    arr.add(
        Bookmark(
            2,
            "YouTube",
            "https://www.youtube.com/",
            null,
            null,
            R.drawable.ic_youtube
        )
    )

    arr.add(
        Bookmark(
            3,
            "Twitter",
            "https://www.twitter.com/",
            null,
            null,
            // R.drawable.decatlon_logo
            R.drawable.ic_twitter
        )
    )

    arr.add(
        Bookmark(
            4,
            "VK",
            "https://www.vk.com/",
            null,
            null,
            R.drawable.ic_vk
        )
    )

    arr.add(
        Bookmark(
            5,
            "Odnoklassniki",
            "https://www.ok.ru/",
            null,
            null,
            R.drawable.ic_odnoklassniki
        )
    )
    when (countryName) {
        "bh" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "http://www.grammarly.com/",
                    // "",
                    null,
                    null,
                    R.drawable.gr_logo,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://www.walmart.ca/en  ",
                    null,
                    null,
                    R.drawable.walmart_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    8,
                    "",
                    "http://canva.com/",
                    "https://static.canva.com/web/images/12487a1e0770d29351bd4ce4f87ec8fe.svg",
                    null,
                    null,
                    true
                )
            )


            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://aliexpress.com/",
                    "https://st.aliexpress.ru/mixer-storage/homePage/snow-homepage/logo-aliexpress.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    10,
                    "",
                    "http://www.overstock.com/",
                    //null,
                    "https://ak1.ostkcdn.com/img/mxc/flag-logo-fix060120.png",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://www.myfonts.com/pages/access-denied",
                    null,
                    null,
                    R.drawable.myfonts_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.walmart.com/",
                    null,
                    null,
                    R.drawable.walmart_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.namecheap.com/",
                    null,
                    null,
                    R.drawable.namecheap_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://vimeo.com/",
                    null,
                    //  "https://f.vimeocdn.com/logo.svg",
                    null,
                    R.drawable.vimeo_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://themeforest.net/",
                    //null,
                    "https://assets.market-storefront.envato-static.com/storefront/assets/logos/envato-market-a5ace93f8482e885ae008eb481b9451d379599dfed24868e52b6b2d66f5cf633.svg",
                    null,
                    null,
                    true
                )
            )
        }

        "en_CA" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://oldnavy.gapcanada.ca/",
                    null,
                    //"https://www.gap.com/Asset_Archive/GPWeb/content/0028/669/369/assets/logo/Gap_logo_MOB_newV2.svg",
                    null,
                    null,
                   // R.drawable.olnavy_logo,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://simons.ca/",
                    null,
              //      "https://imagescdn.simons.ca/imarcom/css/images/logo_share.png",
                    null,
                    null,
                  //  R.drawable.simons_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    8,
                    "",
                    "http://www.grammarly.com/",
                    null,
                    // "",
                    null,
                    R.drawable.gr_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://www.staples.ca/",
                    //  null,
                    "https://cdn.shopify.com/s/files/1/0036/4806/1509/files/logo_wlc_en.svg?v=14170296610798099822",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://www.sephora.com/",
                    null,
                    null,
                    R.drawable.sephora_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://www.gapcanada.ca/",
                    //null,
                    "https://www.gap.com/Asset_Archive/GPWeb/content/0028/669/369/assets/logo/Gap_logo_MOB_newV2.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.ssense.com/en-gb",
                    //null,
                    "https://res.cloudinary.com/ssenseweb/image/upload/v1471963917/web/ssense_logo_v2.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.walmart.ca/en",
                    null,
                    null,
                    R.drawable.walmart_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://aliexpress.com/",
                    "https://st.aliexpress.ru/mixer-storage/homePage/snow-homepage/logo-aliexpress.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "http://www.roots.com/ca",
                    //null,
                    "https://www.roots.com/on/demandware.static/Sites-RootsCA-Site/-/default/dw16781570/images/roots_mobile_logo.svg",
                    null,
                    null,
                    true
                )
            )
//            arr.add(
//                Bookmark(
//                    16,
//                    "",
//                    "https://staples.ca/",
//                    //null,
//                    "https://cdn.shopify.com/s/files/1/0036/4806/1509/files/logo_wlc_en.svg?v=14170296610798099822",
//                    null,
//                    null,
//                    true
//                )
//            )
        }

        "fr" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://www.manomano.fr/",
                    //null,
                    "https://www.manomano.fr/assets/_next/static/images/logo1080-07026c0f92f30805.png",
                    null,
                    null,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://www.decathlon.fr/",
                    null,
                    null,
                    null,
                   // R.drawable.decatlon_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    8,
                    "",
                    "https://www.idmarket.com/",
                    null,
                    //   "https://www.idmarket.com/themes/default-bootstrap/img/logo-idmarket.jpg",
                    null,
                   // null,
                    R.drawable.idmarket_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://www.toolnation.fr/",
                    null,
                    null,
                 //   null,
                    R.drawable.toolnation_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://www.leroymerlin.fr/",
                    null,
                    null,
                    null,
                    //R.drawable.l,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://www.oscaro.com/",
                    null,
                   // "https://oscaro.media/mu/layout/logo/OSCARO_New_UI_White.svg",
                    null,
                 //   null,
                    R.drawable.oscaro_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.marionnaud.fr/",
                    null,
                    //"https://www.marionnaud.fr/medias/logo-marionnaud-noir.gif?context=bWFzdGVyfGltYWdlc3wzODQ3fGltYWdlL2dpZnxpbWFnZXMvaDRkL2g1Yi85Nzk1MDU2ODYxMjE0LmdpZnw5NmRhYjY2YTk4ODc4Yzc4ZDYxYjgzYzZlNWU4YmRmZjYyOWQ2YjdmNDc0NWEyOGJkYzJiODBlYmY4ODU5YTEz",
                    null,
                   null,
                   // R.drawable.marionnaud_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.pierreetvacances.com/be-wl",
                     null,
                  //  "https://photo.pierreetvacances.com/picto/logo/logo_pv.png",
                    null,
                    null,
                  //  R.drawable.peierreetvacances_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://www.alterego-design.fr/",
                    null,
                    //"https://www.alterego-design.fr/skin/frontend/imboutique/alterego/images/logo-sans-baseline.gif",
                    null,
                    null,
                  //  R.drawable.altrego_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://aliexpress.com/",
                    "https://st.aliexpress.ru/mixer-storage/homePage/snow-homepage/logo-aliexpress.svg",
                    null,
                    null,
                    true
                )
            )

        }
        "de" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://www.idealo.de/",
                    null,
                    null,
                 //null,
                    R.drawable.idealo_logo,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://www.roastmarket.de/",
                    //null,
                    "https://www.roastmarket.de/assets/logo.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    8,
                    "",
                    "https://www.whirlstore.de/",
                    //   null,
                    "https://www.whirlstore.de/themes/hidrotienda_theme/img/whirlstore.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://www.idealo.fr/",
                    null,
                    null,
                  //  null,
                    R.drawable.idealo_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://buttinette.com/",
                    null,
                    //  "https://buttinette.com/images/buttinette-logo-BU-DE.png",
                    null,
                    null,
                   // R.drawable.buttinette_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://www.parfumdreams.de/",
                    // null,
                    "https://www.parfumdreams.de/StaticContent/unspecific/img/PDLogo/PDLogo.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.manketech-shop.de/",
                    //null,
                    "https://www.manketech-shop.de/media/vector/1e/26/01/Logo_MankeTech.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.billiger.de/",
                    null,
                    null,
                    null,
                   // R.drawable.billiger_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://www.medimops.de/",
                    //null,
                    "https://www.medimops.de/img/medimops-logo1.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://musicstore.de/",
                    null,
                    // "https://www.musicstore.de/INTERSHOP/static/WFS/MusicStore-MusicStoreShop-Site/-/-/de_DE/images/general/logo.png",
                    null,
               //     null,
                    R.drawable.ms_logo,
                    true
                )
            )

        }
        "hi" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "http://www.grammarly.com/",
                    null,
                    null,
                    R.drawable.gr_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    7,
                    "",
                    "http://canva.com/",
                    "https://static.canva.com/web/images/12487a1e0770d29351bd4ce4f87ec8fe.svg",
                    null,
                    null,
                    true
                )
            )


            arr.add(
                Bookmark(
                    8,
                    "",
                    "http://flipkart.com/",
                    null,
                    //  "https://static-assets-web.flixcart.com/fk-p-linchpin-web/fk-cp-zion/img/flipkart-plus_8d85f4.png",
                    null,
                    null,
                   // R.drawable.flipkart_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://wpengine.com/",
                    null,
                    null,
                   // null,
                    R.drawable.wpengine_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://www.swiggy.com/",
                    //null,
                    "https://res.cloudinary.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_72,h_72/portal/c/logo_2022.png",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://vimeo.com/",
                    null,
                    // "https://f.vimeocdn.com/logo.svg",
                    null,
                    R.drawable.vimeo_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.udemy.com/",
                    //   null,
                    "https://www.udemy.com/staticx/udemy/images/v7/logo-udemy.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.myntra.com/",
                    null,
                    //  "https://constant.myntassets.com/www/data/portal/mlogo.png",
                    null,
                    null,
                    //R.drawable.myntra_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://www.fragrancex.com/",
                    null,
                    // "https://img.fragrancex.com/images/assets/logo/fragrancex_logo.svg?v=3",
                    null,
                 //   null,
                    R.drawable.fragrancex_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "http://www.overstock.com/",
                    // null,
                    "https://ak1.ostkcdn.com/img/mxc/flag-logo-fix060120.png",
                    null,
                    null,
                    true
                )
            )

        }
        "it" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://www.manomano.it/",
                    //null,
                    "https://www.manomano.it/assets/_next/static/images/logo1080-07026c0f92f30805.png",
                    null,
                    null,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://isoladeitesori.it/",
                    null,
                    null,
                   // null,
                    R.drawable.iso_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    8,
                    "",
                    "https://www.unieuro.it/",
                    null,
                    // "https://static1.unieuro.it/medias/sys_master/root/h4f/h72/32818629902366/logoGray.png",
                    null,
                //    null,
                    R.drawable.unieuro_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://www.idealo.de/",
                    null,
                    null,
                   // null,
                    R.drawable.idealo_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://www.svapoebasta.com/",
                    null,
                    //"https://www.svapoebasta.com/img/svapo-e-basta-logo-1551694870.jpg",
                    null,
                  //  null,
                    R.drawable.sv_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://termoclima.srl/",
                    null,
                    //"https://www.termoclima.srl/Resources/Graphics/logo-nuovo-scroll-ai.png",
                    null,
                 //   null,
                    R.drawable.tc_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.farmacieravenna.com/",
                    //null,
                    "https://www.farmacieravenna.com/img/logo-1663666722.jpg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://mascherine.it/",
                    null,
                    null,
                    null,
                   // R.drawable.mascherine_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://www.idroitalia.it/",
                    //null,
                    "https://www.idroitalia.it/themes/hidrotienda_theme/img/idroitalia.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://macchinato.com/",
                    null,
                    //"https://macchinato.com/assets/logo-macchinato.png",
                    null,
                    null,
                  //  R.drawable.macchiato_logo,
                    true
                )
            )

        }

        //"us" ->
         "ru" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://aliexpress.ru/",
                    "https://st.aliexpress.ru/mixer-storage/homePage/snow-homepage/logo-aliexpress.svg",
                    null,
                    null,
                    true
                )
            )


            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://www.vseinstrumenti.ru/",
                    "https://cdn.vseinstrumenti.ru/assets/svg/logo-filled.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    8,
                    "",
                    "https://aptekiplus.ru/",
                    "https://aptekiplus.ru/images/logo/logo-small.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "https://worldoftanks.eu/ru/",
                    null,
                    null,
                    //null,
                    R.drawable.wot_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://www.budzdorov.ru/",
                    null,
                    null,
                   // null,
                    R.drawable.budzdorov_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://lesta.ru/ru/games/mt",
                    null,
                    null,
                   // null,
                    R.drawable.mirtankov_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.chitai-gorod.ru/",
                    null,
                    null,
                  //  null,
                    R.drawable.chitaigorod_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.letu.ru/",
                    "https://www.letu.ru/common/img/logo/Logo_Letoile.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://autopiter.ru/",
                    "https://autopiter.ru/static/app/ui-kit/components/AutopiterLogoLink/autopiter-ru-logo.b71ca.svg",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://poryadok.ru/",
                    //"https://cdn.poryadok.ru/images/main-logo.png",
                    null,
                    null,
                    //null,
                    R.drawable.poryadok_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    16,
                    "",
                    "https://www.ivi.ru/",
                    null,
                    // "https://solea-parent.dfs.ivi.ru/picture/ea003d,ffffff/reposition_iviLogoPlateRounded.svg",
                    null,
                //    null,
                    R.drawable.ivi_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    17,
                    "",
                    "https://www.beautybay.com/",
                    null,
                    null,
                  //  null,
                    R.drawable.beauty_logo,
                    true
                )
            )


        }
        "en" -> {
            arr.add(
                Bookmark(
                    6,
                    "",
                    "https://www.wayfair.com/",
                    null,
                    null,
                    //null,
                    R.drawable.wayfair_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    7,
                    "",
                    "https://www.bhphotovideo.com/",
                    null,
                    null,
               //     null,
                   R.drawable.bhphotovideo_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    8,
                    "",
                    "https://gap.com/",
                    "https://www.gap.com/Asset_Archive/GPWeb/content/0028/669/369/assets/logo/Gap_logo_MOB_newV2.svg",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    9,
                    "",
                    "http://www.grammarly.com/",
                    null,
                    null,
                    R.drawable.gr_logo,
                    true
                )
            )

            arr.add(
                Bookmark(
                    10,
                    "",
                    "https://maurices.com/",
                    null,
                    null,
                    null,
                   // R.drawable.maurices_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    11,
                    "",
                    "https://www.skechers.com/en-us/",
                    null,
                    null,
                    null,
                   // R.drawable.skechers_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    12,
                    "",
                    "https://www.idealo.de/",
                    null,
                    null,
                    //null,
                    R.drawable.idealo_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    13,
                    "",
                    "https://www.womanwithin.com/",
                    "https://cdn-fsly.yottaa.net/5b75bbacf1598a37954bd49c/www.womanwithin.com/v~4b.675/on/demandware.static/Sites-oss-Site/-/default/dwbf6300e4/images/ww/logo.svg?yocs=19_",
                    null,
                    null,
                    true
                )
            )
            arr.add(
                Bookmark(
                    14,
                    "",
                    "https://www.sweetwater.us/",
                    null,
                    null,
                    null,
               //     R.drawable.sw_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    15,
                    "",
                    "https://www.samsclub.com/",
                    null,
                    null,
                    null,
                 //   R.drawable.sams_logo,
                    true
                )
            )
            arr.add(
                Bookmark(
                    16,
                    "",
                    "https://aliexpress.сom/",
                    "https://st.aliexpress.ru/mixer-storage/homePage/snow-homepage/logo-aliexpress.svg",
                    null,
                    null,
                    true
                )
            )
        }
        else -> {
            arr.add(
                Bookmark(
                    6,
                    "Hot Coubs - The Biggest Video Meme Platform",
                    "https://coub.com/",
                    "https://coub-assets.akamaized.net/assets/og/coub_og_image-ac413e288cf569b3fec8bcce869961e530d0f70adef8f94fb47883590e4d57fa.png",
                    null,
                    null,
                    true
                )
            )

            arr.add(
                Bookmark(
                    7,
                    "Amazon.com. Spend less. Smile more.",
                    "https://www.amazon.com/",
                    "http://g-ec2.images-amazon.com/images/G/01/social/api-share/amazon_logo_500500._V323939215_.png",
                    null,
                    null,
                    true
                )
            )


        }
    }

        return arr
    }

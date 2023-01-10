package web.browser.dragon.huawei.utils.other.browser

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

@SuppressWarnings("ResultOfMethodCallIgnored")
class AdBlock(context: Context) {
    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    fun isAd(url: String): Boolean {
        val domain: String
        domain = try {
            getDomain(url)
        } catch (u: URISyntaxException) {
            return false
        }
        return hosts.contains(domain.toLowerCase(locale))
    }

    companion object {
        private const val FILE = "hosts.txt"
        private val hosts: MutableSet<String> = HashSet()

        @SuppressLint("ConstantLocale")
        private val locale = Locale.getDefault()
        fun getHostsDate(context: Context): String {
            val file =
                File(context.getDir("filesdir", Context.MODE_PRIVATE).toString() + "/" + FILE)
            var date = ""
            if (!file.exists()) {
                return ""
            }
            try {
                val `in` = FileReader(file)
                val reader = BufferedReader(`in`)
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    if (line.contains("Date:")) {
                        date = "hosts.txt " + line.substring(2)
                        `in`.close()
                        break
                    }
                }
                `in`.close()
            } catch (i: IOException) {
                Log.w("browser", "Error getting hosts date", i)
            }
            return date
        }

        private fun loadHosts(context: Context) {
            val thread = Thread {
                try {
                    val file = File(
                        context.getDir(
                            "filesdir",
                            Context.MODE_PRIVATE
                        ).toString() + "/" + FILE
                    )
                    val `in` = FileReader(file)
                    val reader = BufferedReader(`in`)
                    var line: String
                    while (reader.readLine().also { line = it } != null) {
                        if (line.startsWith("#")) continue
                        hosts.add(line.toLowerCase(locale))
                    }
                    `in`.close()
                } catch (i: IOException) {
                    Log.w("browser", "Error loading adBlockHosts", i)
                }
            }
            thread.start()
        }

        fun downloadHosts(context: Context) {
            val thread = Thread {
                val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val hostURL = sp.getString(
                    "ab_hosts",
                    "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"
                )
                try {
                    val url = URL(hostURL)
                    val ucon = url.openConnection()
                    ucon.readTimeout = 5000
                    ucon.connectTimeout = 10000
                    val `is` = ucon.getInputStream()
                    val inStream =
                        BufferedInputStream(`is`, 1024 * 5)
                    val tempfile = File(
                        context.getDir(
                            "filesdir",
                            Context.MODE_PRIVATE
                        ).toString() + "/temp.txt"
                    )
                    if (tempfile.exists()) {
                        tempfile.delete()
                    }
                    tempfile.createNewFile()
                    val outStream = FileOutputStream(tempfile)
                    val buff = ByteArray(5 * 1024)
                    var len: Int
                    while (inStream.read(buff).also { len = it } != -1) {
                        outStream.write(buff, 0, len)
                    }
                    outStream.flush()
                    outStream.close()
                    inStream.close()

                    //now remove leading 0.0.0.0 from file
                    val `in` = FileReader(tempfile)
                    val reader = BufferedReader(`in`)
                    val outfile = File(
                        context.getDir(
                            "filesdir",
                            Context.MODE_PRIVATE
                        ).toString() + "/" + FILE
                    )
                    val out = FileWriter(outfile)
                    var line: String
                    while (reader.readLine().also { line = it } != null) {
                        if (line.startsWith("0.0.0.0 ")) {
                            line = line.substring(8)
                        }
                        out.write(
                            """
                                  $line
                                  
                                  """.trimIndent()
                        )
                    }
                    `in`.close()
                    out.close()
                    tempfile.delete()
                    hosts.clear()
                    loadHosts(context) //reload hosts after update
                    Log.w("browser", "AdBlock hosts updated")
                } catch (i: IOException) {
                    Log.w("browser", "Error updating AdBlock hosts", i)
                }
            }
            thread.start()
        }

        @Throws(URISyntaxException::class)
        private fun getDomain(url: String): String {
            var url = url
            url = url.toLowerCase(locale)
            val index = url.indexOf('/', 8) // -> http://(7) and https://(8)
            if (index != -1) {
                url = url.substring(0, index)
            }
            val uri = URI(url)
            val domain = uri.host ?: return url
            return if (domain.startsWith("www.")) domain.substring(4) else domain
        }
    }

    init {
        val file = File(context.getDir("filesdir", Context.MODE_PRIVATE).toString() + "/" + FILE)
        if (!file.exists()) {
            //copy hosts.txt from assets if not available
            try {
                val manager = context.assets
                copyFile(manager.open(FILE), FileOutputStream(file))
                downloadHosts(context) //try to update hosts.txt from internet
            } catch (e: IOException) {
                Log.e("browser", "Failed to copy asset file", e)
            }
        }
        val time = Calendar.getInstance()
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sp.getBoolean("sp_savedata", false)) {
            time.add(Calendar.DAY_OF_YEAR, -7)
        } else {
            time.add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastModified = Date(file.lastModified())
        if (lastModified.before(time.time) || getHostsDate(context) == "") {  //also download again if something is wrong with the file
            //update if file is older than a day
            downloadHosts(context)
        }
        if (hosts.isEmpty()) {
            loadHosts(context)
        }
    }
}

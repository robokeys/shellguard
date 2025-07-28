package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/CommandUUIDGenerator.kt
 * description: UUID generator with multiple strategies for RKCL/shellguard actions (sequential, v1, v3, v4, v5, v6, v7, v8).
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import org.springframework.stereotype.Component
import tech.robd.shellguard.rkcl.config.UuidProperties
import java.net.NetworkInterface
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.UuidCreator.NAMESPACE_DNS
/**
 * [📌 Point: UUID Generator wiring]
 * Provides a configured UUID generator for the application.
 * Can be extended for more runtime strategies or custom ID tracking.
 */
@Component
class CommandUuidGenerator(
    private val uuidProperties: UuidProperties
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        fun fromString(name: String?): CommandIdUtil.UuidMode =
            CommandIdUtil.UuidMode.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: CommandIdUtil.UuidMode.V4 // Default to V4 if unknown or null
    }

    val mode = fromString(uuidProperties.mode)

    init {
        logger.info { "[CommandUuidGenerator] Configuring UUID mode: $mode, prefix: ${uuidProperties.prefix}" }
        CommandIdUtil.configure(mode, uuidProperties.prefix)
    }

    fun generateId(): String {
        val id = CommandIdUtil.generateId()
        logger.info("[CommandUuidGenerator] Generated ID: $id (mode=$mode)")
        return id
    }
}


object CommandIdUtil {
    private val logger = KotlinLogging.logger {}

    /**
     * This version is not set up to enable DCE security UUIDs ie v2 - so it gives a default implementation
     */
    enum class UuidMode {
        SEQUENTIAL,      // incrementing ints or strings
        V1,              // time-based
        //V2,              // DCE Security (rarely used)
        V3,              // name-based MD5
        V4,              // random
        V5,              // name-based SHA1
        V6,
        V7,
        V8,              // custom/traceable
    }

    private var mode: UuidMode = UuidMode.V4
    private var prefix: String? = "cmd"

    private val counter = AtomicInteger(0)
    private var namespace: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    fun configure(mode: UuidMode, prefix: String? = null) {
        logger.info { "[CommandIdUtil] Configuring: mode=$mode, prefix=$prefix" }

        this.mode = mode
        if (prefix != null) this.prefix = prefix
    }

    // [📌 Section: CommandIdUtil core logic]
    /**
     * Implements all supported UUID generation modes.
     * Add new UUID strategies here or extend for distributed tracing, audit, or debugging.
     */
    fun generateId(): String {
        val pfx = prefix ?: "obj-cmd"
        return when (mode.toString()) {
            UuidMode.SEQUENTIAL.toString() -> {
                val id = "$pfx-${counter.incrementAndGet().toString().padStart(3, '0')}"
                logger.info { "[CommandIdUtil] (SEQUENTIAL) Generated ID: $id (counter=${counter.get()})" }
                id.toString()
            }
            UuidMode.V1.toString() -> {
                val uuidV1 = UuidCreator.getTimeBased().toString()
                logger.info { "[CommandIdUtil] (PROD) Generated (V1) UUID: $uuidV1" }
                uuidV1.toString()
            }
//            UuidMode.V2.toString()  -> {
//                val domain : Byte = 2
//                val localIdentifier : Int = 12345
//                val uuidV2 = UuidCreator.getDceSecurity(domain, localIdentifier).toString()
//                logger.info { "[CommandIdUtil] (PROD) Generated (V2)  UUID: $uuidV2" }
//                uuidV2.toString()
//            }
            UuidMode.V3.toString()  -> {
                val uuidV3 = UuidCreator.getNameBasedMd5(NAMESPACE_DNS, "example.com").toString()
                logger.info { "[CommandIdUtil] (PROD) Generated (V3)  UUID: $uuidV3" }
                uuidV3.toString()
            }
            UuidMode.V4.toString()  -> {
                val uuid = UUID.randomUUID().toString()
                logger.info { "[CommandIdUtil] (PROD) Generated random UUID: $uuid" }
                uuid.toString()
            }
            UuidMode.V5.toString()  -> {
                val uuidV5 = UuidCreator.getNameBasedSha1(NAMESPACE_DNS, "example.com")
                logger.info { "[CommandIdUtil] (PROD) Generated (V5)  UUID: $uuidV5" }
                uuidV5.toString()
            }
            UuidMode.V6.toString()  -> {
                val uuidV6 = UuidCreator.getTimeOrdered() //
                logger.info { "[CommandIdUtil] (PROD) Generated (V6)  UUID: $uuidV6" }
                uuidV6.toString()
            }
            UuidMode.V7.toString()  -> {
                val uuidV7 = UuidCreator.getTimeOrderedEpoch() //
                logger.info { "[CommandIdUtil] (PROD) Generated (V7)  UUID: $uuidV7" }
                uuidV7.toString()
            }
            UuidMode.V8.toString()  -> {
                val uuid = UuidV8Generator.generateUUIDv8()
                logger.info { "[CommandIdUtil] (V8) Generated traceable UUID: $uuid" }
                uuid
            }
            else -> {
                // kotlin not smart enough to spot this is exhaustive yet
                val uuid = UUID.randomUUID().toString()
                logger.info { "[CommandIdUtil] (PROD) Generated random UUID: $uuid" }
                uuid
            }
        }
    }
    // [/📌 Section: CommandIdUtil core logic]

    fun generateId(mode: UuidMode, prefix: String): String {
        configure(mode, prefix)
        return generateId()
    }
}
/**
* [🧩 Section: UUIDv8 traceable implementation]
* Implements custom, traceable UUIDv8 with partial node fingerprint and time.
* Extend or refactor for better device fingerprinting, compliance, or interop.
*/
object UuidV8Generator {
    private val random = SecureRandom()

    // Replace this with a stable local node identity, or leave random for true randomness
    private fun getNodeId(): ByteArray {
        // Try to get the first non-loopback MAC address as a device fingerprint
        val mac = NetworkInterface.getNetworkInterfaces()
            .toList()
            .firstOrNull { !it.isLoopback && it.hardwareAddress != null }
            ?.hardwareAddress
        if (mac != null) return mac

        // Fallback: Use hostname hash
        val hostname = try {
            java.net.InetAddress.getLocalHost().hostName
        } catch (ex: Exception) {
            "unknown"
        }
        return MessageDigest.getInstance("SHA-256").digest(hostname.toByteArray()).take(6).toByteArray()
    }

    // CRC24 used in the Arduino code, but we will just use the first 3 bytes of SHA-256 of node ID for simplicity
    private fun calculateNodeFingerprint(): ByteArray {
        return getNodeId().take(3).toByteArray()
    }

    fun generateUUIDv8(): String {
        val uuid = ByteArray(16)
        val fingerprint = calculateNodeFingerprint()

        // Set bytes 0-2 to node fingerprint
        uuid[0] = fingerprint[0]
        uuid[1] = fingerprint[1]
        uuid[2] = fingerprint[2]

        // Set bytes 3-5 to timestamp (tenths of a second, modulo 14 days)
        val nowTenths = (Instant.now().toEpochMilli() / 100) % 12096000 // 14 days in tenths of seconds
        uuid[3] = ((nowTenths shr 16) and 0xFF).toByte()
        uuid[4] = ((nowTenths shr 8) and 0xFF).toByte()
        uuid[5] = (nowTenths and 0xFF).toByte()

        // Fill bytes 6-15 with random
        fillBytesWithRandom(random, uuid, 6, 10)
        // Set version to 8 in high nibble of byte 6
        uuid[6] = ((uuid[6].toInt() and 0x0F) or 0x80).toByte()

        // Set RFC4122 variant in byte 8 (bits 6 and 7 to 1 0)
        uuid[8] = ((uuid[8].toInt() and 0x3F) or 0x80).toByte()

        // Format as UUID string
        return "%02x%02x%02x-%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x".format(
            uuid[0], uuid[1], uuid[2],
            uuid[3], uuid[4], uuid[5],
            uuid[6], uuid[7],
            uuid[8], uuid[9],
            uuid[10], uuid[11], uuid[12], uuid[13], uuid[14], uuid[15]
        )
    }

    fun fillBytesWithRandom(random: java.util.Random, target: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && length >= 0 && offset + length <= target.size) {
            "Range out of bounds: offset=$offset, length=$length, array.size=${target.size}"
        }
        val temp = ByteArray(length)
        random.nextBytes(temp)
        for (i in 0 until length) {
            target[offset + i] = temp[i]
        }
    }
    /* [/🧩 Section: UUIDv8 traceable implementation] */

}

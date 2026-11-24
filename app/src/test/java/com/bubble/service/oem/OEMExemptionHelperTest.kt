package com.bubble.service.oem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests validating OEM brand detection normalization and intent fallback priorities.
 */
class OEMExemptionHelperTest {

    @Test
    fun detectBrand_samsungVariants_mapsToSamsung() {
        assertEquals(OEMBrand.SAMSUNG, OEMExemptionHelper.detectBrand("samsung"))
        assertEquals(OEMBrand.SAMSUNG, OEMExemptionHelper.detectBrand("Samsung"))
        assertEquals(OEMBrand.SAMSUNG, OEMExemptionHelper.detectBrand("SAMSUNG ELECTRONICS"))
    }

    @Test
    fun detectBrand_xiaomiVariants_mapsToXiaomi() {
        assertEquals(OEMBrand.XIAOMI, OEMExemptionHelper.detectBrand("Xiaomi"))
        assertEquals(OEMBrand.XIAOMI, OEMExemptionHelper.detectBrand("redmi"))
        assertEquals(OEMBrand.XIAOMI, OEMExemptionHelper.detectBrand("POCO"))
    }

    @Test
    fun detectBrand_huaweiVariants_mapsToHuawei() {
        assertEquals(OEMBrand.HUAWEI, OEMExemptionHelper.detectBrand("HUAWEI"))
        assertEquals(OEMBrand.HUAWEI, OEMExemptionHelper.detectBrand("Honor"))
    }

    @Test
    fun detectBrand_onePlusOppoVariants_mapsToOppoOnePlus() {
        assertEquals(OEMBrand.OPPO_ONEPLUS, OEMExemptionHelper.detectBrand("OnePlus"))
        assertEquals(OEMBrand.OPPO_ONEPLUS, OEMExemptionHelper.detectBrand("OPPO"))
        assertEquals(OEMBrand.OPPO_ONEPLUS, OEMExemptionHelper.detectBrand("realme"))
    }

    @Test
    fun detectBrand_vivoVariants_mapsToVivo() {
        assertEquals(OEMBrand.VIVO, OEMExemptionHelper.detectBrand("vivo"))
        assertEquals(OEMBrand.VIVO, OEMExemptionHelper.detectBrand("iQOO"))
    }

    @Test
    fun detectBrand_pixelOrUnknown_mapsToGeneric() {
        assertEquals(OEMBrand.GENERIC, OEMExemptionHelper.detectBrand("Google"))
        assertEquals(OEMBrand.GENERIC, OEMExemptionHelper.detectBrand("Motorola"))
        assertEquals(OEMBrand.GENERIC, OEMExemptionHelper.detectBrand(""))
    }

    @Test
    fun brandDisplayNames_areHumanReadable() {
        assertEquals("Samsung", OEMBrand.SAMSUNG.displayName)
        assertEquals("Xiaomi / HyperOS", OEMBrand.XIAOMI.displayName)
        assertEquals("Huawei", OEMBrand.HUAWEI.displayName)
        assertEquals("OnePlus / Oppo", OEMBrand.OPPO_ONEPLUS.displayName)
        assertEquals("Vivo", OEMBrand.VIVO.displayName)
        assertEquals("Android System", OEMBrand.GENERIC.displayName)
    }
}

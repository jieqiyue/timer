package com.timetracking.app.ui.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview annotations for different device configurations
 */

// Phone - Portrait
@Preview(
    name = "Phone - Portrait",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showSystemUi = true
)
annotation class PhonePortraitPreview

// Phone - Landscape
@Preview(
    name = "Phone - Landscape",
    device = "spec:width=640dp,height=360dp,dpi=480",
    showSystemUi = true
)
annotation class PhoneLandscapePreview

// Small Tablet - Portrait
@Preview(
    name = "Small Tablet - Portrait",
    device = "spec:width=600dp,height=960dp,dpi=240",
    showSystemUi = true
)
annotation class SmallTabletPortraitPreview

// Small Tablet - Landscape
@Preview(
    name = "Small Tablet - Landscape",
    device = "spec:width=960dp,height=600dp,dpi=240",
    showSystemUi = true
)
annotation class SmallTabletLandscapePreview

// Large Tablet - Portrait
@Preview(
    name = "Large Tablet - Portrait",
    device = "spec:width=840dp,height=1280dp,dpi=240",
    showSystemUi = true
)
annotation class LargeTabletPortraitPreview

// Large Tablet - Landscape
@Preview(
    name = "Large Tablet - Landscape",
    device = "spec:width=1280dp,height=840dp,dpi=240",
    showSystemUi = true
)
annotation class LargeTabletLandscapePreview

// All device previews
annotation class AllDevicePreviews {
    companion object {
        @PhonePortraitPreview
        @PhoneLandscapePreview
        @SmallTabletPortraitPreview
        @SmallTabletLandscapePreview
        @LargeTabletPortraitPreview
        @LargeTabletLandscapePreview
        annotation class All
    }
}

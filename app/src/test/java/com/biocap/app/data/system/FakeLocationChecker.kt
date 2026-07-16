package com.biocap.app.data.system

class FakeLocationChecker(var locationEnabled: Boolean = true) : LocationChecker {
    override fun isLocationEnabled(): Boolean = locationEnabled
}

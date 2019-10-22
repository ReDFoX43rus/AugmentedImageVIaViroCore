package com.liberaid.virocoretest

import android.util.Log
import com.viro.core.ARAnchor
import com.viro.core.ARNode
import com.viro.core.ARScene
import com.viro.core.Vector

open class ARSetupSceneListener() : ARScene.Listener {
    private var inited = false

    override fun onTrackingInitialized() {}
    override fun onTrackingUpdated(trackingState: ARScene.TrackingState?, trackingStateReason: ARScene.TrackingStateReason?) {
        if(trackingState != null && trackingState == ARScene.TrackingState.NORMAL && !inited){
            inited = true
        }
    }
    override fun onAmbientLightUpdate(p0: Float, p1: Vector?) {}
    override fun onAnchorUpdated(p0: ARAnchor?, p1: ARNode?) {}
    override fun onAnchorFound(p0: ARAnchor?, p1: ARNode?) {
        Log.d("JUSTDOIT", "anchor found ${p0?.anchorId}")
    }
    override fun onAnchorRemoved(p0: ARAnchor?, p1: ARNode?) {
        Log.d("JUSTDOIT", "anchor removed ${p0?.anchorId}")
    }
}
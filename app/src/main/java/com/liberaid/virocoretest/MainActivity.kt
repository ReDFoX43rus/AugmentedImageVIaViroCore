package com.liberaid.virocoretest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.viro.core.*

class MainActivity : AppCompatActivity() {

    private lateinit var viroView: ViroView
    private lateinit var arScene: ARScene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viroView = ViroViewARCore(this, object : ViroViewARCore.StartupListener {
            override fun onSuccess() {
                arScene = ARScene()
                val imageId = setupImageRecognition()
                arScene.setListener( object : ARSetupSceneListener() {
                    override fun onAnchorFound(p0: ARAnchor?, p1: ARNode?) {
                        super.onAnchorFound(p0, p1)

                        if(p0?.anchorId == imageId)
                            playVideo(p1!!)
                    }
                })

                viroView.scene = arScene
            }

            override fun onFailure(p0: ViroViewARCore.StartupError?, p1: String?) {
                throw RuntimeException("Cannot init ViroView")
            }
        })

        setContentView(viroView)
    }

    private fun playVideo(node: ARNode) {
        val videoTexture = VideoTexture(viroView.viroContext, Uri.parse("file:///android_asset/just_do_it.mp4"))

        val material = Material().apply {
            diffuseTexture = videoTexture
            chromaKeyFilteringColor = Color.GREEN
            isChromaKeyFilteringEnabled = true
        }

        val scale = 1f
        val surface = Quad(1.6f * scale, .9f * scale)
        surface.materials = mutableListOf(material)

        node.addChildNode(Node().apply {
            setRotation(Quaternion.makeRotationFromTo(Vector(0f, 1f, 0f), Vector(0f, 0f, -1f)))
            setScale(Vector(1f, 1f, 1f).scale(.15f))
            setPosition(Vector(-.05f, 0f, 0f))
            geometry = surface
        })

        videoTexture.loop = true
        videoTexture.play()
        videoTexture.isMuted = true
    }

    private fun setupImageRecognition(): String {
        val template = getBitmapFromAssets("just_do_it_transparent.png")
        val imageTarget = ARImageTarget(template, ARImageTarget.Orientation.Up, .1f)
        Log.d("JUSTDOIT", "imageTarget=${imageTarget.id}")

        arScene.addARImageTarget(imageTarget)

        return imageTarget.id
    }

    private fun getBitmapFromAssets(filename: String): Bitmap {
        val inputStream = assets.open(filename)
        return BitmapFactory.decodeStream(inputStream)
    }

    override fun onStart() {
        super.onStart()
        viroView.onActivityStarted(this)
    }

    override fun onResume() {
        super.onResume()
        viroView.onActivityResumed(this)
    }

    override fun onPause() {
        super.onPause()
        viroView.onActivityPaused(this)
    }

    override fun onStop() {
        super.onStop()
        viroView.onActivityStopped(this)
    }
}

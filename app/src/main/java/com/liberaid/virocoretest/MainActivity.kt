package com.liberaid.virocoretest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.viro.core.*
import timber.log.Timber


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
                            placeModel(p1!!)
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

    private fun placeModel(node: ARNode) {
        Timber.d("Image detected=${SystemClock.elapsedRealtime()}")

        val glbFilename = "rectangle.glb"

        Object3D().apply {
            node.addChildNode(this)

            loadModel(viroView.viroContext, Uri.parse("file:///android_asset/$glbFilename"), Object3D.Type.GLB, object : AsyncObject3DListener {
                override fun onObject3DLoaded(p0: Object3D?, p1: Object3D.Type?) {
                    Timber.d("Model loaded")
                }

                override fun onObject3DFailed(p0: String?) {
                    Timber.d("Error loading model")
                }
            })

            Timber.d("Model is attached to node")

            val ambient = AmbientLight(Color.WHITE.toLong(), 1000.0f)
            addLight(ambient)
        }

        /*val videoTexture = VideoTexture(viroView.viroContext, Uri.parse("file:///android_asset/just_do_it.mp4"))

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
        videoTexture.isMuted = true*/
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

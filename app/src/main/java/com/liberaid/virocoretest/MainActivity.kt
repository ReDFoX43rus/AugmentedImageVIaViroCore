package com.liberaid.virocoretest

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.viro.core.*
import com.viro.core.Vector
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viroView: ViroView
    private lateinit var arScene: ARScene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viroView = ViroViewARCore(this, object : ViroViewARCore.StartupListener {
            override fun onSuccess() {
                arScene = ARScene()
                viroView.scene = arScene

                arScene.setListener(ARSetupSceneListener {
                    setupScene()
                })
            }

            override fun onFailure(p0: ViroViewARCore.StartupError?, p1: String?) {
                throw RuntimeException("Cannot init ViroView")
            }
        })

        setContentView(viroView)
    }

    private fun setupScene() {
        setupLights()
        createObjectAtPosition(Vector(0f, 0f, -1f))
    }

    private fun setupLights() {
        // Add some lights to the scene; this will give the Android's some nice illumination.
        val rootNode = arScene.getRootNode()
        val lightPositions = ArrayList<Vector>()
        lightPositions.add(Vector(-10f, 10f, 1f))
        lightPositions.add(Vector(10f, 10f, 1f))

        val intensity = 300f
        val lightColors = mutableListOf<Int>()
        lightColors.add(Color.WHITE)
        lightColors.add(Color.WHITE)

        for (i in lightPositions.indices) {
            val light = OmniLight()
            light.color = lightColors.get(i).toLong()
            light.position = lightPositions[i]
            light.attenuationStartDistance = 20f
            light.attenuationEndDistance = 30f
            light.intensity = intensity
            rootNode.addLight(light)
        }
    }

    private fun createObjectAtPosition(position: Vector) {
        val object3D = Object3D()
        object3D.setPosition(position)
        object3D.setRotation(Quaternion.makeRotationFromTo(Vector(0f, 1f, 0f), Vector(0f, 0f, -1f)))

        val scale = .08f
        object3D.setScale(Vector(scale, scale, scale))

        arScene.rootNode.addChildNode(object3D)

        object3D.loadModel(
            viroView.getViroContext(),
            Uri.parse("file:///android_asset/tv.obj"),
            Object3D.Type.OBJ,
            object : AsyncObject3DListener {
                override fun onObject3DLoaded(`object`: Object3D, type: Object3D.Type) {
                    val videoTexture = VideoTexture(viroView.viroContext, Uri.parse("file:///android_asset/just_do_it.mp4"))

                    val material = Material().apply {
                        diffuseTexture = videoTexture
                        chromaKeyFilteringColor = Color.GREEN
                        isChromaKeyFilteringEnabled = false
                    }

                    object3D.geometry.materials = Arrays.asList(material)

                    videoTexture.apply {
                        isMuted = true
                        loop = true
                        play()
                    }
                }

                override fun onObject3DFailed(s: String) {}
            })
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

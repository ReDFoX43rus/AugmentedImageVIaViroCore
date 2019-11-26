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
import com.viro.core.Vector
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext = job + Dispatchers.Default

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

                        setupLights()
//                        createObjectAtPosition(Vector(0f, 0f, -1f), false)
//                        createObjectAtPosition(Vector(0f, 0f, -1f), true)
                        createMenAtPosition(Vector(0f, 0f, -1f))
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

    private fun createObjectAtPosition(position: Vector, animated: Boolean) {
        val object3D = Object3D()
        object3D.setPosition(position)
        object3D.setRotation(Quaternion.makeRotationFromTo(Vector(0f, 1f, 0f), Vector(0f, 0f, -1f)))

        val scale = .2f
        object3D.setScale(Vector(scale, scale, scale))
        object3D.setRotation(Quaternion.makeRotationFromTo(Vector(0f, 0f, 1f), Vector(0f, 1f, 0f)))

        arScene.rootNode.addChildNode(object3D)

        val filename = if(animated) "rectangle_animated.glb" else "rectangle.glb"
        val type = Object3D.Type.GLB

        Timber.d("Create .glb, animated=$animated")

        object3D.loadModel(
            viroView.getViroContext(),
            Uri.parse("file:///android_asset/$filename"),
            type,
            object : AsyncObject3DListener {
                override fun onObject3DLoaded(`object`: Object3D, type: Object3D.Type) {
                    Timber.d(".glb loaded, animated=$animated")

                    val animations = `object`.animationKeys
                    Timber.d("Animations=$animations")
                }
                override fun onObject3DFailed(s: String) {
                    Timber.d(".glb failed to load, animated=$animated, err=$s")
                }
            })
    }

    private fun createMenAtPosition(position: Vector) {

        val zeroScale = Vector(0f, 0f, 0f)

        val scale = 0.3f
        val sizeScale = Vector(scale, scale, scale)

        launchCatching({
            val men = (1..80).map {
                Object3D().apply {
                    setPosition(position)

                    setScale(zeroScale)

                    arScene.rootNode.addChildNode(this)

                    val filename = if(it < 10)
                        "man_obj/m_27-T1_00000$it.obj"
                    else "man_obj/m_27-T1_0000$it.obj"
                    val type = Object3D.Type.OBJ

                    suspendCoroutine<Unit> {
                        loadModel(viroView.viroContext, Uri.parse("file:///android_asset/$filename"), type, object : AsyncObject3DListener {
                            override fun onObject3DLoaded(p0: Object3D?, p1: Object3D.Type?) {
                                Timber.d("Loaded $filename")
                                it.resume(Unit)
                            }

                            override fun onObject3DFailed(p0: String?) {
                                Timber.d("$filename failed to load")
                                it.resumeWithException(RuntimeException("$filename failed to load"))
                            }
                        })
                    }
                }
            }

            var current = 0
            while(true){
                for(i in men.indices){
                    if(i != current){
                        men[i].setScale(zeroScale)
                    } else {
                        men[i].setScale(sizeScale)
                    }
                }

                Timber.d("Activated man #$current")

                delay(50)
                current++
                current %= men.size
            }
        }, { _, t ->
            throw t
        })
    }

    private fun placeModel(node: ARNode) {
        Timber.d("Image detected=${SystemClock.elapsedRealtime()}")

        val filename = "rectangle.obj"
        val type = Object3D.Type.OBJ

        Object3D().apply {
            node.addChildNode(this)

            loadModel(viroView.viroContext, Uri.parse("file:///android_asset/$filename"), type, object : AsyncObject3DListener {
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

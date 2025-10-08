package com.example.bestappintheworld.commons

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.math.abs

class DiceGame : ApplicationAdapter() {
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var camera: PerspectiveCamera
    private lateinit var diceModel: Model
    private lateinit var diceInstance: ModelInstance

    private var angleX = 0f
    private var angleY = 0f
    private var angleZ = 0f
    private var rotating = false

    override fun create() {
        modelBatch = ModelBatch()

        environment = Environment().apply {
            set(ColorAttribute.createAmbientLight(1f, 1f, 1f, 1f))
            add(DirectionalLight().set(Color.WHITE, Vector3(-1f, -0.8f, -0.2f)))
        }

        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            position.set(3f, 3f, 3f)
            lookAt(0f, 0f, 0f)
            near = 1f
            far = 300f
            update()
        }

        val objLoader = ObjLoader()
        diceModel = objLoader.loadModel(Gdx.files.internal("models/d12.obj"))
        diceInstance = ModelInstance(diceModel)

        Gdx.input.inputProcessor = object : com.badlogic.gdx.InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                rollDice()
                return true
            }
        }
    }

    private fun rollDice() {
        angleX = MathUtils.random(0f, 720f)
        angleY = MathUtils.random(0f, 720f)
        angleZ = MathUtils.random(0f, 720f)
        rotating = true
    }

    override fun render() {
        ScreenUtils.clear(Color.BLACK)

        if (rotating) {
            diceInstance.transform.rotate(Vector3.X, angleX * Gdx.graphics.deltaTime)
            diceInstance.transform.rotate(Vector3.Y, angleY * Gdx.graphics.deltaTime)
            diceInstance.transform.rotate(Vector3.Z, angleZ * Gdx.graphics.deltaTime)

            angleX *= 0.95f
            angleY *= 0.95f
            angleZ *= 0.95f

            if (abs(angleX) < 0.1f && abs(angleY) < 0.1f && abs(angleZ) < 0.1f) {
                rotating = false
                // Aqui você pode calcular qual face está "pra cima", se quiser
            }
        }

        modelBatch.begin(camera)
        modelBatch.render(diceInstance, environment)
        modelBatch.end()
    }

    override fun dispose() {
        modelBatch.dispose()
        diceModel.dispose()
    }
}
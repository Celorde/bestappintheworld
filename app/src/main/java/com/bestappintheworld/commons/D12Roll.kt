//package com.example.bestuiintheworld.commons
//
//import kotlin.random.Random
//
//object D12Roll(ctx: Context) : RajawaliRenderer(ctx) {
//    val model = ModelLoader.obj.loadModel(Gdx.files.internal("d12.obj"))
//    val instance = ModelInstance(model)
//
//    override fun render() {
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
//        modelBatch.begin(camera)
//        modelBatch.render(instance, environment)
//        modelBatch.end()
//    }
//
//    fun rollDice() {
//        val x = Random.nextFloat() * 360 * 3
//        val y = Random.nextFloat() * 360 * 3
//        instance.transform.setToRotation(Vector3.X, x).rotate(Vector3.Y, y)
//    }
//
//    fun rollDice(): String {
//        return Random.nextInt(-1, 11).toString()
//    }
//}

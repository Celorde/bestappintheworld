package com.example.bestappintheworld.fragments.extrainfo

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bestappintheworld.R
import com.example.bestappintheworld.commons.Utils
import com.example.bestappintheworld.commons.Utils.plateList
import com.example.bestappintheworld.commons.showToast
import com.example.bestappintheworld.components.OnItemClickListener
import com.bestappintheworld.databinding.DialogDrawClubBinding
import com.bestappintheworld.databinding.DialogFavoriteColorPickerBinding
import com.bestappintheworld.databinding.DialogFavoriteDishBinding
import com.bestappintheworld.databinding.DialogFavoritePlateBinding
import com.bestappintheworld.databinding.DialogFootConversionBinding
import com.bestappintheworld.databinding.DialogFootSizeMeasureBinding
import com.bestappintheworld.databinding.DialogStreamersOptionsBinding
import com.bestappintheworld.databinding.FragmentExtraInfoBinding
import com.example.bestappintheworld.ui.IngredientView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ExtraInfoFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentExtraInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExtraInfoViewModel by viewModel()

    private var sensorManager: SensorManager? = null
    private var colorPickerDialog: DialogFavoriteColorPickerBinding? = null
    private var footSizeConversionDialog: DialogFootConversionBinding? = null

    private var footAnswerTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExtraInfoBinding.inflate(inflater, container, false)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_GAME
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupStateListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()

        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_GAME
        )

        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun setupStateListeners() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.apply {
                    insertStreamerEditText.setText(uiState.streamerName)
                    insertFavoriteColorEditText.setText(uiState.favoriteColor)
                    confirmFavoriteColorEditText.setText(uiState.confirmFavoriteColor)
                    favoritePlateEditText.setText(uiState.favoritePlate)
                    confirmFavoriteColorTextView.isVisible = uiState.favoriteColor.isNotEmpty()
                    confirmFavoriteColorEditText.isVisible = uiState.favoriteColor.isNotEmpty()
                    favoriteDishTextView.isVisible = uiState.favoritePlate.isNotEmpty()
                    favoriteDishEditText.isVisible = uiState.favoritePlate.isNotEmpty()
                    footSizeConversionDialog?.apply {
                        answerContainer.isVisible = uiState.answers != null
                        footAnswer1Button.text = uiState.answers?.answer1?.text
                        footAnswer2Button.text = uiState.answers?.answer2?.text
                        footAnswer3Button.text = uiState.answers?.answer3?.text
                        footAnswer4Button.text = uiState.answers?.answer4?.text
                        footShoeSizeTextView.text = resources.getQuantityString (
                            R.plurals.foot_size_shoe_size_text,
                            uiState.footErrorCount,
                            uiState.shoeSizeInCentimeters,
                            uiState.baseFootInCentimeters
                        )
                    }
                    footSizeEditText.setText(uiState.footSizeText)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.extraInfoEvent.collect { event ->
                when (event) {
                    is ExtraInfoEvent.WrongStreamerSelected -> clearAllFields()
                    is ExtraInfoEvent.ShowError -> showToast(event.message)
                    else -> {}
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            insertStreamerEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showStreamersOptions()
                }
            }

            insertFavoriteColorEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showColorOptions(isConfirm = false)
                }
            }

            confirmFavoriteColorEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showColorOptions(isConfirm = true)
                }
            }

            heartClubEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showClubToDraw()
                }
            }

            favoritePlateEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showFavoritePlateDialog()
                }
            }

            favoriteDishEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showFavoriteDishDialog()
                }
            }

            footSizeEditText.let { it ->
                it.keyListener = null

                it.setOnClickListener {
                    showFootSizeMeasureDialog()
                }
            }
        }
    }

    private fun showStreamersOptions() {
        val dialogView = DialogStreamersOptionsBinding.inflate(layoutInflater)
        var selectStreamerDialog: AlertDialog? = null

        dialogView.apply {
            val adapter = StreamerAdapter(object : OnItemClickListener {
                override fun onItemClick(item: String) {
                    selectStreamerDialog?.dismiss()
                    viewModel.validateSelectedStreamer(item)
                }
            })

            streamersListRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }

        selectStreamerDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_streamer))
            .setView(dialogView.root)
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun showColorOptions(isConfirm: Boolean) {
        colorPickerDialog = DialogFavoriteColorPickerBinding.inflate(layoutInflater)
        colorPickerDialog?.colorEditText?.keyListener = null
        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_favorite_color))
            .setView(colorPickerDialog?.root)
            .setPositiveButton("OK") { _, _ ->
                viewModel.updateSelectedColor(colorPickerDialog?.colorEditText?.text.toString(), isConfirm)
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun showClubToDraw() {
        val clubDrawDialog = DialogDrawClubBinding.inflate(layoutInflater)
        clubDrawDialog.apply {
            clubEditText.keyListener = null
            eraseButton.setOnClickListener {
                clubDrawingView.clearDrawing()
            }

            selectColorButton.setOnClickListener {
                colorPickerDialog = DialogFavoriteColorPickerBinding.inflate(layoutInflater)
                colorPickerDialog?.colorEditText?.keyListener = null
                var dialog: AlertDialog? = null

                dialog = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.heart_club_color))
                    .setView(colorPickerDialog?.root)
                    .setPositiveButton("OK") { _, _ ->
                        val corEmHexa = colorPickerDialog?.colorEditText?.text.toString()
                        val corEmInt = corEmHexa.toColorInt()

                        clubDrawingView.setStrokeColor(corEmInt)
                    }
                    .setNegativeButton("Cancelar") { _, _ -> }
                    .show()
            }
        }

        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.heart_club))
            .setView(clubDrawDialog.root)
            .setPositiveButton("Cancelar") { _, _ -> }
            .setNegativeButton("OK") { _, _ ->
                showToast("Foto de perfil Atualizada com sucesso.")
                binding.heartClubEditText.isVisible = false
                binding.heartClubTextView.isVisible = false
            }
            .show()
    }

    private fun showFavoritePlateDialog() {
        val favoritePlateDialog = DialogFavoritePlateBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        var remainingPlates = plateList.toMutableMap()

        var leftDraftedPlate = drawNewPlate(remainingPlates)
        var rightDraftedPlate = drawNewPlate(remainingPlates)

        favoritePlateDialog.apply {
            leftPlateImageView.apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        leftDraftedPlate,
                        null
                    )
                )
                setOnClickListener {
                    if (remainingPlates.isEmpty()) {
                        viewModel.setFavoritePlate(plateList[leftDraftedPlate])
                        dialog?.dismiss()
                        return@setOnClickListener
                    }

                    leftDraftedPlate = drawNewPlate(remainingPlates)

                    setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            leftDraftedPlate,
                            null
                        )
                    )
                }
            }
            rightPlateImageView.apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        rightDraftedPlate,
                        null
                    )
                )

                setOnClickListener {
                    if (remainingPlates.isEmpty()) {
                        viewModel.setFavoritePlate(plateList[rightDraftedPlate])
                        dialog?.dismiss()
                        return@setOnClickListener
                    }

                    rightDraftedPlate = drawNewPlate(remainingPlates)

                    setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            rightDraftedPlate,
                            null
                        )
                    )
                }
            }
        }

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.favorite_plate))
            .setView(favoritePlateDialog.root)
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun drawNewPlate(remainingPlates: MutableMap<Int, String>): Int {
        var leftDraftedPlate = remainingPlates.keys.random()
        remainingPlates.remove(leftDraftedPlate)

        return leftDraftedPlate
    }

    private fun showFavoriteDishDialog() {
        var okCount = 0
        var ingredientCounter = 0

        val favoriteDishDialog = DialogFavoriteDishBinding.inflate(layoutInflater)

        favoriteDishDialog.addIngredientButton.setOnClickListener {
            ingredientCounter++

            val newIngredienteField = IngredientView(
                context = requireContext(),
                ingredientIndex = ingredientCounter
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            favoriteDishDialog.containerLinearLayout.addView(newIngredienteField)
        }

        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.favorite_dish))
            .setView(favoriteDishDialog.root)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            favoriteDishDialog.apply {
                if (favoriteDishEditText.text.toString().lowercase() == "morango do amor") {
                    showToast("Ah pelo amor de Deus.")
                    favoriteDishEditText.setText("")
                    return@setOnClickListener
                }

                if (favoriteDishEditText.text.isEmpty()) {
                    showToast("Ué e a receita, criatura, cadê?")
                } else if (okCount == 0) {
                    addIngredientButton.isVisible = true
                    containerLinearLayout.isVisible = true

                    okCount++
                } else {
                    when (ingredientCounter) {
                        0 -> showToast("Ué, não tem ingredientes nessa receita? Mentiroso.")
                        in 1..3 -> showToast("Essa receita é muito simples. Precisa de pelo menos 4 ingredientes.")
                        in 9..Int.MAX_VALUE -> {
                            showToast("Calmaí Jacquin, essa receita é muito complexa. Máximo de 8 ingredientes.")
                            ingredientCounter = 0
                            resetDish(favoriteDishDialog)
                        }
                        else -> {
                            if (validateIngredients(favoriteDishDialog).not()) {
                                ingredientCounter = 0
                                return@setOnClickListener
                            }

                            binding.favoriteDishEditText.setText(favoriteDishEditText.text)
                            dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun resetDish(favoriteDishDialog: DialogFavoriteDishBinding) {
        favoriteDishDialog.apply {
            favoriteDishEditText.setText("")
            containerLinearLayout.removeAllViews()
        }
    }

    private fun validateIngredients(favoriteDishDialog: DialogFavoriteDishBinding): Boolean {
        val ingredientsContainer = favoriteDishDialog.containerLinearLayout.children.toList()
        var haveSpice = false

        for (ingredientView in ingredientsContainer) {
            val ingredientElement = (ingredientView as IngredientView).children.toList()[0] as ConstraintLayout
            for (component in ingredientElement.children.toList()) {
                if (component is EditText) {
                    val ingredientHint = component.hint.toString()

                    if (ingredientHint.contains("100g").not()) {
                        val ingredient = component.text.toString()

                        if (ingredient.lowercase() in Utils.spiceList.toList()) {
                            haveSpice = true
                        }

                        if (ingredient.lowercase() in Utils.ingredientBlacklist.toList()) {
                            showToast("$ingredient é proibido em nossa sociedade. Escolha outra receita.")
                            resetDish(favoriteDishDialog)

                            return false
                        }
                    }
                }
            }
        }

        if (haveSpice.not()) {
            showToast("Faltou Tompero. Adicione sabor à sua receita")
            resetDish(favoriteDishDialog)

            return false
        }

        return true
    }

    private fun showFootSizeMeasureDialog() {
        val footSizeMeasureDialog = DialogFootSizeMeasureBinding.inflate(layoutInflater)
        var conversionDialog: AlertDialog? = null
        var dialog: AlertDialog? = null

        footSizeMeasureDialog.dontKnowMyFootButton.setOnClickListener {
            dialog?.dismiss()
            footSizeConversionDialog = DialogFootConversionBinding.inflate(layoutInflater)

            footSizeConversionDialog?.apply {
                footShoeAnswerButton.setOnClickListener {
                    startTimer(conversionDialog)
                    viewModel.calculateFootSizeInCentimeters(footSizeEditText.text.toString())
                }

                footAnswer1Button.apply {
                    setOnClickListener {
                        conversionDialog?.dismiss()
                        checkValidAnswer(text.toString())
                    }
                }
                footAnswer2Button.apply {
                    setOnClickListener {
                        conversionDialog?.dismiss()
                        checkValidAnswer(text.toString())
                    }
                }
                footAnswer3Button.apply {
                    setOnClickListener {
                        conversionDialog?.dismiss()
                        checkValidAnswer(text.toString())
                    }
                }
                footAnswer4Button.apply {
                    setOnClickListener {
                        conversionDialog?.dismiss()
                        checkValidAnswer(text.toString())
                    }
                }
            }

            conversionDialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.foot_size_dialog))
                .setView(footSizeConversionDialog?.root)
                .setPositiveButton("OK") { _, _, -> }
                .setNegativeButton("Cancelar") { _, _ -> }
                .show()
        }

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.foot_size_dialog))
            .setView(footSizeMeasureDialog.root)
            .setPositiveButton("OK") { _, _, -> }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun checkValidAnswer(buttonText: String) {
        val timeRemaining = footSizeConversionDialog?.answerTimerTextView?.text.toString().toDouble()
        if (timeRemaining > 7) {
            showToast("Não deu tempo de fazer Regra de 3. Por segurança, faça novamente.")
            footAnswerTimer?.cancel()
            return
        }

        footAnswerTimer?.cancel()

        val allAnswers = listOf(
            viewModel.uiState.value.answers?.answer1,
            viewModel.uiState.value.answers?.answer2,
            viewModel.uiState.value.answers?.answer3,
            viewModel.uiState.value.answers?.answer4
        )

        val isCorrect = (allAnswers.find { it?.text.equals(buttonText, ignoreCase = true) })?.isCorrect == true

        if (isCorrect) {
            viewModel.setFootValue(buttonText)
            binding.footSizeEditText.setText(buttonText)
        } else {
            //TODO A cada 2 segundos troca as respostas
            //TODO Passar um AD porque eu preciso pagar o almoço
            viewModel.updateFootError()
            showToast("Resposta errada, tente novamente.")
        }
    }

    private fun startTimer(conversionDialog: AlertDialog?) {
        val totalTime = 10_000L
        val shuffleInterval = 2_000L
        val interval = 100L

        object : CountDownTimer(totalTime, shuffleInterval) {
            override fun onTick(millisUntilFinished: Long) {
                viewModel.shuffleFootAnswers()
            }

            override fun onFinish() {
            }
        }.start()

        footAnswerTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000.0
                footSizeConversionDialog?.answerTimerTextView?.text =
                    String.format(Locale.getDefault(), "%.1f", seconds)
            }

            override fun onFinish() {
                conversionDialog?.dismiss()
                viewModel.updateFootError()
                showToast("O tempo acabou, tente novamente.")
            }
        }.start()
    }

    private fun clearAllFields() {
        binding.apply {
            insertStreamerEditText.setText("")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val nx = viewModel.calculatePointerX(event.values)
            val ny = viewModel.calculatePointerY(event.values)

            colorPickerDialog?.colorPickerTray?.setPointerPosition(nx, ny)

            val selectedColor = colorPickerDialog?.colorPickerTray?.getSelectedColor() ?: 0

            var colorText = String.format("#%06X", 0xFFFFFF and selectedColor)

            if (viewModel.uiState.value.favoriteColor.isNotEmpty()) {
                val originalColor = colorText.toColorInt()
                val hsv = FloatArray(3)
                Color.colorToHSV(originalColor, hsv)

                colorText = "H: %.2f, S: %.2f%%, V: %.2f%%".format(hsv[0], hsv[1] * 100, hsv[2] * 100)
            }

            colorPickerDialog?.colorEditText?.setText(colorText)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //not used
    }
}

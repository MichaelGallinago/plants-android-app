package net.micg.plantcare.presentation.alarmCreation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.SeekBar
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import net.micg.plantcare.R
import net.micg.plantcare.databinding.FragmentAlarmCreationBinding
import net.micg.plantcare.di.appComponent
import net.micg.plantcare.di.viewModel.ViewModelFactory
import net.micg.plantcare.presentation.models.Alarm
import net.micg.plantcare.utils.AlarmCreationUtils
import net.micg.plantcare.utils.AlarmCreationUtils.calculateIntervalInMillis
import net.micg.plantcare.utils.AlarmCreationUtils.getCurrentCalendar
import net.micg.plantcare.utils.AlarmCreationUtils.getTypeName
import net.micg.plantcare.utils.FirebaseUtils
import net.micg.plantcare.utils.InsetsUtils.addTopInsetsMarginToCurrentView
import net.micg.plantcare.utils.TypeLabelUtils
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Calendar.getInstance
import javax.inject.Inject
import kotlin.math.max

class AlarmCreationFragment : Fragment(R.layout.fragment_alarm_creation) {
    @Inject
    lateinit var factory: ViewModelFactory

    private val binding: FragmentAlarmCreationBinding by viewBinding()
    private val viewModel: AlarmCreationViewModel by viewModels { factory }

    private var isEditing = false
    private var editingId = 0L
    private var editingIsEnabled = true

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpEdgeToEdgeForCurrentFragment()
        setUpListeners(findNavController())
        setUpFragment()
        setUpArguments()
    }

    private fun setUpArguments() = arguments?.let {
        AlarmCreationFragmentArgs.fromBundle(it)
    }?.let { args ->
        with(args) {
            binding.intervalBar.progress = max(interval - 1, 0)
            binding.nameEditText.setText(plantName)

            isEditing = isEdition
            editingId = id
            editingIsEnabled = isEnabled

            context?.let { ctx ->
                FirebaseUtils.logEvent(ctx, FirebaseUtils.ALARM_CREATION_ENTERS, Bundle().apply {
                    putString("from_screen", fragmentName)
                })
            }
        }
    }

    private fun setUpEdgeToEdgeForCurrentFragment() {
        addTopInsetsMarginToCurrentView(binding.confirmButton)
        addTopInsetsMarginToCurrentView(binding.cancelButton)
    }

    private fun setUpFragment() = with(binding) {
        with(intervalBar) {
            setOnSeekBarChangeListener(IntervalBarChangeListener())
            max = 29
            progress = 0
        }

        intervalValue.addTextChangedListener(IntervalValueTextWatcher())

        with(viewModel.timeStorage) {
            with(getCurrentCalendar()) {
                year = get(YEAR)
                month = get(MONTH)
                dayOfMonth = get(DAY_OF_MONTH)
                hourOfDay = get(HOUR_OF_DAY)
                minute = get(MINUTE)
            }

            with(dateSelector) {
                text = dateFormated
                setOnClickListener { pickDate() }
            }

            with(timeSelector) {
                text = timeFormated
                setOnClickListener { pickTime() }
            }
        }
    }

    private fun setUpListeners(navController: NavController) = with(binding) {
        cancelButton.setOnClickListener {
            navController.popBackStack()
        }

        confirmButton.setOnClickListener {
            saveAlarm()
            navController.navigate(
                AlarmCreationFragmentDirections.actionAlarmCreationFragmentToAlarmsFragment()
            )
        }
    }

    private fun pickDate() = with(getCurrentCalendar()) {
        DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            ::setDate,
            get(YEAR),
            get(MONTH),
            get(DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = timeInMillis
            show()
        }
    }

    private fun setDate(picker: DatePicker, year: Int, month: Int, day: Int) =
        with(viewModel.timeStorage) {
            this.year = year
            this.month = month
            dayOfMonth = day
            binding.dateSelector.text = dateFormated
        }

    private fun pickTime() = with(getCurrentCalendar()) {
        TimePickerDialog(
            requireContext(),
            R.style.CustomTimePickerDialog,
            ::setTime,
            get(HOUR_OF_DAY),
            get(MINUTE),
            true
        ).show()
    }

    private fun setTime(picker: TimePicker, hour: Int, minute: Int) = with(viewModel.timeStorage) {
        hourOfDay = hour
        this.minute = minute
        binding.timeSelector.text = timeFormated
    }

    private fun saveAlarm() = with(binding) {
        val name = nameEditText.text.toString()
        val interval = viewModel.interval
        val type = if (radioWatering.isChecked) 0 else 1

        if (isEditing) {
            viewModel.updateData(
                editingId,
                name,
                type.toByte(),
                dateInMillis,
                calculateIntervalInMillis(interval),
                editingIsEnabled
            )

            context?.let { ctx ->
                FirebaseUtils.logEvent(ctx, FirebaseUtils.EDITED_NOTIFICATIONS, Bundle().apply {
                    putString("type", getTypeName(type))
                    putString("name", name)
                    putString("date", AlarmCreationUtils.convertTimeToString(dateInMillis))
                    putLong("interval", interval)
                })
            }
        } else {
            viewModel.insert(
                name,
                type.toByte(),
                dateInMillis,
                calculateIntervalInMillis(interval)
            )

            context?.let { ctx ->
                FirebaseUtils.logEvent(ctx, FirebaseUtils.CREATED_NOTIFICATIONS, Bundle().apply {
                    putString("type", getTypeName(type))
                    putString("name", name)
                    putString("date", AlarmCreationUtils.convertTimeToString(dateInMillis))
                    putLong("interval", interval)
                })
            }
        }
    }

    private val dateInMillis
        get() = getInstance().apply {
            timeInMillis = 0L
            with(viewModel.timeStorage) {
                set(YEAR, year)
                set(MONTH, month)
                set(DAY_OF_MONTH, dayOfMonth)
                set(HOUR_OF_DAY, hourOfDay)
                set(MINUTE, minute)
            }
        }.timeInMillis

    private inner class IntervalBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            with(viewModel) {
                if (isUpdating) return

                isUpdating = true
                interval = progress.toLong() + SEEK_BAR_MIN
                binding.intervalValue.setText("$interval")
                isUpdating = false
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private inner class IntervalValueTextWatcher() : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(viewModel) {
                if (isUpdating) return

                isUpdating = true
                val value = s.toString().toIntOrNull() ?: 0
                if (value in SEEK_BAR_MIN..binding.intervalBar.max) {
                    binding.intervalBar.progress = value
                }
                isUpdating = false
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (viewModel.isUpdating) return

            var interval = s.toString().toIntOrNull().run {
                if (this == null) return
                coerceAtLeast(1)
            }

            with(binding.intervalBar) { progress = (interval - SEEK_BAR_MIN).coerceAtMost(max) }

            viewModel.isUpdating = true
            val intervalText = "$interval"
            with(binding.intervalValue) {
                setText(intervalText)
                setSelection(intervalText.length)
            }
            viewModel.isUpdating = false
            viewModel.interval = interval.toLong()
        }
    }

    companion object {
        private const val SEEK_BAR_MIN: Int = 1
    }
}

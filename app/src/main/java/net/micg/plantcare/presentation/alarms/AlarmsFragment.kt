package net.micg.plantcare.presentation.alarms

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import net.micg.plantcare.R
import net.micg.plantcare.databinding.FragmentAlarmsBinding
import net.micg.plantcare.di.viewModel.ViewModelFactory
import net.micg.plantcare.di.appComponent
import net.micg.plantcare.presentation.models.TimeConverter
import net.micg.plantcare.presentation.models.TimeLocalization
import net.micg.plantcare.utils.FirebaseUtils
import net.micg.plantcare.utils.InsetsUtils.addTopInsetsMarginToCurrentView
import javax.inject.Inject

class AlarmsFragment : Fragment(R.layout.fragment_alarms) {
    @Inject
    lateinit var factory: ViewModelFactory

    private val binding: FragmentAlarmsBinding by viewBinding()
    private val viewModel: AlarmsViewModel by viewModels { factory }

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpEdgeToEdgeForCurrentFragment()
        setUpAdapter()
        setUpNavigation()

        context?.let { ctx ->
            FirebaseUtils.logEvent(ctx, FirebaseUtils.ALARMS_ENTERS)
        }
    }

    private fun setUpEdgeToEdgeForCurrentFragment() = addTopInsetsMarginToCurrentView(binding.label)

    private fun setUpAdapter() = AlarmsAdapter(
        onToggleClick = { alarm, isEnabled -> viewModel.update(isEnabled, alarm) },
        TimeConverter(TimeLocalization(requireContext())),
        onEditClick = { alarm ->
            val interval = TimeConverter.millisecondsToDays(alarm.intervalInMillis)
            navigateToAlarmCreation(alarm.name, interval, true, alarm.id, alarm.isEnabled)
        },
        onDeleteClick = { alarm -> viewModel.delete(alarm) }
    ).also {
        ItemTouchHelper(TouchHelperCallback(it)).attachToRecyclerView(binding.recycler)

        with(binding.recycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = it

            ContextCompat.getDrawable(context, R.drawable.divider_shape)?.also {
                with(DividerItemDecoration(context, DividerItemDecoration.VERTICAL)) {
                    setDrawable(it)
                    addItemDecoration(this)
                }
            }
        }

        viewModel.allAlarms.observe(viewLifecycleOwner) { alarms -> it.submitList(alarms) }
    }

    private fun setUpNavigation() {
        binding.addAlarmButton.setOnClickListener {
            navigateToAlarmCreation("", 0, false)
        }
    }

    private fun navigateToAlarmCreation(
        name: String, interval: Int, isEdit: Boolean, id: Long = 0L, isEnabled: Boolean = true
    ) = findNavController().navigate(
        AlarmsFragmentDirections.actionAlarmsFragmentToAlarmCreationFragment(
            name, interval, "Alarms", isEdit, id, isEnabled
        )
    )

    private inner class TouchHelperCallback(val adapter: AlarmsAdapter) :
        ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = with(adapter) {
            with(viewHolder.adapterPosition) {
                viewModel.delete(currentList[this])
                removeItem(this)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ) = false

        @Deprecated(
            "This project doesn't provide for moving items as a useful or mandatory feature"
        )
        private fun moveItem(viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
            adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        }
    }
}

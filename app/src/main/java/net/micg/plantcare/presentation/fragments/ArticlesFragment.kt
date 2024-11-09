package net.micg.plantcare.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import net.micg.plantcare.R
import net.micg.plantcare.databinding.FragmentArticlesBinding
import net.micg.plantcare.presentation.adapters.ArticlesAdapter

class ArticlesFragment : Fragment(R.layout.fragment_articles) {
    private val binding: FragmentArticlesBinding by viewBinding()
    private val articlesAdapter = ArticlesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ebat", "you're in the articles fragment")
        with(binding.recycler) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articlesAdapter
        }

        //articlesAdapter.submitValue(getScheduleForThisDay())
    }
}
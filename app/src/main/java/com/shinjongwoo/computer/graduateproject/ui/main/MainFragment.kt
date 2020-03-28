package com.shinjongwoo.computer.graduateproject.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.shinjongwoo.computer.graduateproject.MainActivity
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView1.text = "진짜 텍스트가 바뀌었는지 확인"
        button2.setOnClickListener {
            (activity as MainActivity)?.changedView()
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }
}

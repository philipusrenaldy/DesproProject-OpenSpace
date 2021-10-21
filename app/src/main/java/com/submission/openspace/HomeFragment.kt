package com.submission.openspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {



    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.home_fragment, container, false)
        setHasOptionsMenu(true)

        return rootView
    }

        companion object {
            var TAG = HomeFragment::class.java.simpleName
            private const val ARG_POSITION: String = "position"

            //        const val REQUEST_VIDEO_CAPTURE = 1
            fun newInstance(): HomeFragment {
                val fragment = HomeFragment()
                val args = Bundle()
                args.putInt(ARG_POSITION, 1)
                fragment.arguments = args
                return fragment
            }
        }
    }
package iit.uvip.psysuite.core.ui.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import iit.uvip.psysuite.core.databinding.FragmentResultsListBinding

import org.albaspazio.core.filesystem.getFileNamesList

/**
 * A fragment representing a list of Items.
 */
class ResultsFragment : Fragment() {

    var relPath:String = Environment.DIRECTORY_DOWNLOADS
    var filesList:MutableList<ResultFileEntry> = mutableListOf()
    private lateinit var binding: FragmentResultsListBinding

    lateinit var listAdapter:ResultsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            relPath = it.getString("RES_RELPATH").toString()
        }

        getFileNamesList(relPath, listOf(".txt")).map{
            filesList.add(ResultFileEntry(it, false))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentResultsListBinding.inflate(inflater, container, false)

        listAdapter             = ResultsRecyclerViewAdapter(filesList)
        binding.list.adapter       = listAdapter
        binding.list.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.btSendResults.setOnClickListener {

        }

        binding.swSelectAll.setOnCheckedChangeListener { _, b ->
            listAdapter.selectAll(b)
        }
    }
}
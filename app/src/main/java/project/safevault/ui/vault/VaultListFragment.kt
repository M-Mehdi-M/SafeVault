package project.safevault.ui.vault

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import project.safevault.R
import project.safevault.databinding.FragmentVaultListBinding
import project.safevault.models.ItemCategory

class VaultListFragment : Fragment() {

    private var _binding: FragmentVaultListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by activityViewModels()
    private lateinit var adapter: VaultItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVaultListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupCategoryFilter()
        setupFab()
        observeItems()
    }

    private fun setupRecyclerView() {
        adapter = VaultItemAdapter { item ->
            val bundle = Bundle().apply { putLong("itemId", item.id) }
            findNavController().navigate(R.id.action_vaultList_to_detail, bundle)
        }
        binding.rvVaultItems.adapter = adapter
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.currentList[position]
                viewModel.deleteItem(item)
                Snackbar.make(binding.root, R.string.vault_item_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.vault_undo) {
                        viewModel.addItem(item.title, viewModel.decryptContent(item.encryptedContent), item.category)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvVaultItems)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryFilter() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when {
                checkedIds.contains(R.id.chipIdCard) -> ItemCategory.ID_CARD
                checkedIds.contains(R.id.chipPassword) -> ItemCategory.PASSWORD
                checkedIds.contains(R.id.chipNote) -> ItemCategory.NOTE
                checkedIds.contains(R.id.chipReceipt) -> ItemCategory.RECEIPT
                else -> null
            }
            viewModel.setFilter(category)
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_vaultList_to_addItem)
        }
        binding.rvVaultItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.fabAdd.shrink() else if (dy < 0) binding.fabAdd.extend()
            }
        })
    }

    private fun observeItems() {
        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvVaultItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


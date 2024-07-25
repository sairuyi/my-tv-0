package com.lizongying.mytv0

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.lizongying.mytv0.databinding.ChannelBinding
import com.lizongying.mytv0.models.TVModel

class ChannelFragment : Fragment() {
    private var _binding: ChannelBinding? = null
    private val binding get() = _binding

    private val handler = Handler(Looper.getMainLooper())
    private val delay: Long = 3000
    private var channel = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChannelBinding.inflate(inflater, container, false)
        return _binding?.root?.apply {
            visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding?.let { binding ->
            val application = requireActivity().applicationContext as MyTVApplication

            binding.channel.layoutParams.width = application.px2Px(binding.channel.layoutParams.width)
            binding.channel.layoutParams.height = application.px2Px(binding.channel.layoutParams.height)

            val layoutParams = binding.channel.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = application.px2Px(binding.channel.marginTop)
            layoutParams.marginEnd = application.px2Px(binding.channel.marginEnd)
            binding.channel.layoutParams = layoutParams

            binding.content.textSize = application.px2PxFont(binding.content.textSize)
            binding.time.textSize = application.px2PxFont(binding.time.textSize)

            binding.main.layoutParams.width = application.shouldWidthPx()
            binding.main.layoutParams.height = application.shouldHeightPx()
        }
    }

    fun show(tvViewModel: TVModel) {
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
        binding?.content?.text = (tvViewModel.tv.id.plus(1)).toString()
        view?.visibility = View.VISIBLE
        handler.postDelayed(hideRunnable, delay)
    }

    fun show(channel: String) {
        binding?.let { binding ->
            if (binding.content.text.length > 1) {
                return
            }
            this.channel = "${binding.content.text}$channel".toInt()
            handler.removeCallbacks(hideRunnable)
            handler.removeCallbacks(playRunnable)
            if (binding.content.text.isEmpty()) {
                binding.content.text = channel
                view?.visibility = View.VISIBLE
                handler.postDelayed(playRunnable, delay)
            } else {
                binding.content.text = "${binding.content.text}$channel"
                handler.postDelayed(playRunnable, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (view?.visibility == View.VISIBLE) {
            handler.postDelayed(hideRunnable, delay)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
    }

    private val hideRunnable = Runnable {
        binding?.content?.text = ""
        view?.visibility = View.GONE
    }

    private val playRunnable = Runnable {
        (activity as? MainActivity)?.play(channel - 1)
        handler.postDelayed(hideRunnable, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
        _binding = null
    }

    companion object {
        private const val TAG = "ChannelFragment"
    }
}
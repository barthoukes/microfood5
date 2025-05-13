package connect.hha.grpc_client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hha.grpc.GrpcChannelFactory
import com.hha.service.PingPongService
import tech.hha.microfood.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var pingPongService: PingPongService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val channel = GrpcChannelFactory.createChannel()
        pingPongService = PingPongService(channel)

        binding.buttonFirst.setOnClickListener {
            val response = pingPongService.ping("Bart")
            Log.i("FirstFragment", "Response: $response")
            // You could also update UI here with the response
        }
    }

    override fun onDestroyView() {
        pingPongService.shutdown()
        _binding = null
        super.onDestroyView()
    }
}
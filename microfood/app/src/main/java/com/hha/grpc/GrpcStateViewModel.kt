package com.hha.grpc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// A data class to hold the communication state, which the UI will observe.
data class GrpcState(val sentAngle: Float, val confirmedAngle: Float)

/**
 * Manages the visual state of gRPC communication.
 * Tracks the number of sent and confirmed messages and exposes them as angles for the UI.
 */
class GrpcStateViewModel : ViewModel()
{

   // The LiveData that the UI will observe. It emits a state object with both angles.
   private val _grpcState = MutableLiveData<GrpcState>()
   val grpcState: LiveData<GrpcState> = _grpcState

   private var sentCounter = 0
   private var confirmedCounter = 0

   // The angle increment for each message. 360 / 6 = 60 degrees.
   private val DEGREES_PER_MESSAGE = 60f

   init
   {
      // Initialize with an idle state.
      _grpcState.value = GrpcState(0f, 0f)
   }

   /**
    * Call this right BEFORE you make a gRPC call.
    */
   @Synchronized
   fun messageSent()
   {
      sentCounter++
      updateState()
   }

   /**
    * Call this in the `onSuccess` or `finally` block of your gRPC call.
    */
   @Synchronized
   fun messageConfirmed()
   {
      confirmedCounter++
      updateState()
   }

   /**
    * Pushes the new state to the LiveData observers.
    */
   private fun updateState()
   {
      val sentAngle = sentCounter * DEGREES_PER_MESSAGE
      val confirmedAngle = confirmedCounter * DEGREES_PER_MESSAGE
      _grpcState.value = GrpcState(sentAngle, confirmedAngle)
   }
}

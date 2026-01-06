package com.example.basiccalculator.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.Decimal -> enterDecimal()
        }
    }

    private fun enterNumber(number: Int) {
        if (_state.value.operation == null) {
            if (_state.value.firstNumber.length < 15) {
                _state.update { it.copy(firstNumber = it.firstNumber + number) }
            }
        } else {
            if (_state.value.secondNumber.length < 15) {
                _state.update { it.copy(secondNumber = it.secondNumber + number) }
            }
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        if (_state.value.firstNumber.isNotEmpty()) {
            _state.update { it.copy(operation = operation) }
        }
    }

    private fun delete() {
        if (_state.value.secondNumber.isNotEmpty()) {
            _state.update { it.copy(secondNumber = it.secondNumber.dropLast(1)) }
        } else if (_state.value.operation != null) {
            _state.update { it.copy(operation = null) }
        } else if (_state.value.firstNumber.isNotEmpty()) {
            _state.update { it.copy(firstNumber = it.firstNumber.dropLast(1)) }
        }
    }

    private fun clear() {
        _state.update { CalculatorState() }
    }

    private fun enterDecimal() {
        if (_state.value.operation == null) {
            if (!_state.value.firstNumber.contains(".")) {
                _state.update { it.copy(firstNumber = it.firstNumber + ".") }
            }
        } else {
            if (!_state.value.secondNumber.contains(".")) {
                _state.update { it.copy(secondNumber = it.secondNumber + ".") }
            }
        }
    }

    private fun calculate() {
        val first = _state.value.firstNumber.toDoubleOrNull()
        val second = _state.value.secondNumber.toDoubleOrNull()
        val operation = _state.value.operation

        if (first != null && second != null && operation != null) {
            val result = when (operation) {
                is CalculatorOperation.Add -> first + second
                is CalculatorOperation.Subtract -> first - second
                is CalculatorOperation.Multiply -> first * second
                is CalculatorOperation.Divide -> if (second != 0.0) first / second else Double.NaN
                is CalculatorOperation.Modulo -> first % second
            }
            
            _state.update {
                it.copy(
                    firstNumber = result.toString(),
                    secondNumber = "",
                    operation = null
                )
            }
        }
    }
}

data class CalculatorState(
    val firstNumber: String = "",
    val secondNumber: String = "",
    val operation: CalculatorOperation? = null
) {
    val displayText: String
        get() {
            val opSymbol = when (operation) {
                is CalculatorOperation.Add -> "+"
                is CalculatorOperation.Subtract -> "-"
                is CalculatorOperation.Multiply -> "*"
                is CalculatorOperation.Divide -> "/"
                is CalculatorOperation.Modulo -> "%"
                else -> ""
            }
            return "${firstNumber}${opSymbol}${secondNumber}"
        }
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Calculate : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
}

sealed class CalculatorOperation {
    object Add : CalculatorOperation()
    object Subtract : CalculatorOperation()
    object Multiply : CalculatorOperation()
    object Divide : CalculatorOperation()
    object Modulo : CalculatorOperation()
}
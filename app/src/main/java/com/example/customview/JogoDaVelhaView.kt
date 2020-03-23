package com.example.customview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.R
import android.graphics.*
import android.view.*
import android.view.MotionEvent
import android.view.GestureDetector
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Parcelable
import android.os.Parcel


class JogoDaVelhaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) :
    View(context, attrs, style) {

    private var mTamanho: Int = 0
    private var mVez: Int = 0
    private var mTabuleiro = Array(3) { IntArray(3) }
    private var rect: RectF = RectF()
    private lateinit var mPaint: Paint
    private lateinit var mImageX: Bitmap
    private lateinit var mImageO: Bitmap

    private var mDetector: GestureDetector? = null

    var mListener: JogoDaVelhaListener? = null


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.style = Paint.Style.FILL
        mImageX = BitmapFactory.decodeResource(resources, R.drawable.ic_delete)
        mImageO = BitmapFactory.decodeResource(resources, R.drawable.ic_input_add)

        mDetector = GestureDetector(
            this.context, VelhaTouchListener()
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mTamanho = when (layoutParams.width) {
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                (TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 48f,
                    resources.displayMetrics
                ) * 3).toInt()
            }

            ViewGroup.LayoutParams.WRAP_CONTENT ->
                Math.min(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.getSize(heightMeasureSpec)
                )
            else -> layoutParams.width
        }
        setMeasuredDimension(mTamanho, mTamanho)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val quadrante = (mTamanho / 3).toFloat()
        val tamanhoF = mTamanho.toFloat()
        // Desenhando as linhas
        mPaint!!.color = Color.BLACK
        mPaint!!.strokeWidth = 3F

        // Verticais
        canvas.drawLine(quadrante, 0F, quadrante, tamanhoF, mPaint)
        canvas.drawLine(quadrante * 2, 0F, quadrante * 2, tamanhoF, mPaint)
        // Horizontais
        canvas.drawLine(0F, quadrante, tamanhoF, quadrante, mPaint)
        canvas.drawLine(0F, quadrante * 2, tamanhoF, quadrante * 2, mPaint)

        mTabuleiro.forEachIndexed { rowIndex, rowValue ->

            rowValue.forEachIndexed { columnIndex, columnValue ->
                val x = (columnIndex * quadrante)
                val y = (rowIndex * quadrante)
                rect.set(x, y, x + quadrante, y + quadrante)

                if (columnValue == XIS) {
                    canvas.drawBitmap(mImageX, null, rect, null)

                } else if (columnValue == BOLA) {
                    canvas.drawBitmap(mImageO, null, rect, null)
                }

            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mDetector!!.onTouchEvent(event)
    }

    inner class VelhaTouchListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            var vencedor = gameOver()
            if (event.action == MotionEvent.ACTION_UP && vencedor == VAZIO) {
                val quadrante = mTamanho / 3
                val linha = (event.y / quadrante).toInt()
                val coluna = (event.x / quadrante).toInt()
                if (mTabuleiro!![linha][coluna] == VAZIO) {
                    mTabuleiro!![linha][coluna] = mVez
                    mVez = if (mVez == XIS) BOLA else XIS
                    invalidate()
                    vencedor = gameOver()
                    if (vencedor != VAZIO) {
                        if (mListener != null) {
                            mListener!!.fimDeJogo(vencedor)
                        }
                    }
                    return true
                }
            }
            return super.onSingleTapUp(event)
        }
    }

    private fun gameOver(): Int {
        // Horizontais
        if (ganhou(mTabuleiro[0][0], mTabuleiro[0][1], mTabuleiro[0][2])) {
            return mTabuleiro[0][0]
        }
        if (ganhou(mTabuleiro[1][0], mTabuleiro[1][1], mTabuleiro[1][2])) {
            return mTabuleiro[1][0]
        }
        if (ganhou(mTabuleiro[2][0], mTabuleiro[2][1], mTabuleiro[2][2])) {
            return mTabuleiro[2][0]
        }
        // Verticais
        if (ganhou(mTabuleiro[0][0], mTabuleiro[1][0], mTabuleiro[2][0])) {
            return mTabuleiro[0][0]
        }
        if (ganhou(mTabuleiro[0][1], mTabuleiro[1][1], mTabuleiro[2][1])) {
            return mTabuleiro[0][1]
        }
        if (ganhou(mTabuleiro[0][2], mTabuleiro[1][2], mTabuleiro[2][2])) {
            return mTabuleiro[0][2]
        }
        // Diagonais
        if (ganhou(mTabuleiro[0][0], mTabuleiro[1][1], mTabuleiro[2][2])) {
            return mTabuleiro[0][0]
        }
        if (ganhou(mTabuleiro[0][2], mTabuleiro[1][1], mTabuleiro[2][0])) {
            return mTabuleiro[0][2]
        }
        // Existem espaços vazios
        if (mTabuleiro.flatMap { it.asList() }.any { it == VAZIO }) {
            return VAZIO
        }
        return EMPATE
    }

    private fun ganhou(a: Int, b: Int, c: Int): Boolean {
        return a == b && b == c && a != VAZIO
    }

    interface JogoDaVelhaListener {
        fun fimDeJogo(vencedor: Int)
    }

    fun reiniciarJogo() {
        mTabuleiro = Array(3) { IntArray(3) }
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val p = super.onSaveInstanceState()
        return EstadoJogo(p, mVez, mTabuleiro)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val estado = state as EstadoJogo
        super.onRestoreInstanceState(estado.superState)
        mVez = estado.vez
        mTabuleiro = estado.tabuleiro
        invalidate()
    }

    class EstadoJogo : BaseSavedState {
        var vez: Int = 0
        var tabuleiro: Array<IntArray>


        constructor(p: Parcelable?, vez: Int, tabuleiro: Array<IntArray>) : super(p) {
            this.vez = vez
            this.tabuleiro = tabuleiro
        }

        constructor(p: Parcel?) : super(p) {
            vez = p?.readInt() ?: XIS
            tabuleiro = Array(3) { IntArray(3) }
            tabuleiro.forEach { p?.readIntArray(it) }

        }

        override fun writeToParcel(parcel: Parcel?, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel?.writeInt(vez)
            tabuleiro.forEach { parcel?.writeIntArray(it) }
        }

        companion object CREATOR : Parcelable.Creator<EstadoJogo> {
            override fun createFromParcel(source: Parcel?) = EstadoJogo(source)
            override fun newArray(size: Int) = arrayOf<EstadoJogo>()
        }
    }


    companion object {
        private val VAZIO = 0
        val EMPATE = 3

        val XIS = 1
        val BOLA = 2
    }

}
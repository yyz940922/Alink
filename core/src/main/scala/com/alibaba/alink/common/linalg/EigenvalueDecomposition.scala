package com.alibaba.alink.common.linalg

import breeze.linalg.{eig, eigSym}
import com.alibaba.alink.common.linalg.BreezeUtils.toBreezeMatrix

/** Eigenvalues and eigenvectors of a real matrix.
  * <P>
  * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is
  * diagonal and the eigenvector matrix V is orthogonal.
  * <P>
  * If A is not symmetric, then the eigenvalue matrix D is block diagonal
  * with the real eigenvalues in 1-by-1 blocks and any complex eigenvalues,
  * lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda].  The
  * columns of V represent the eigenvectors in the sense that A*V = V*D,
  *i.e. A.times(V) equals V.times(D).  The matrix V may be badly
  * conditioned, or even singular, so the validity of the equation
  * A = V*D*inverse(V) depends upon V.cond().
  * */
class EigenvalueDecomposition(val A: DenseMatrix) {

    val symmetric: Boolean = A.isSymmetric()
    val eigSym.EigSym(symeval, symevec) = if (symmetric) eigSym(toBreezeMatrix(A)) else null
    val eig.Eig(eval, ceval, evec) = if (!symmetric) eig(toBreezeMatrix(A)) else null

    /**
      * Return the eigenvector matrix
      */
    def getV(): DenseMatrix = {
        if (symmetric) BreezeUtils.fromBreezeMatrix(symevec)
        else BreezeUtils.fromBreezeMatrix(evec)
    }

    /**
      * Return the real parts of the eigenvalues
      */
    def getRealEigenvalues(): DenseVector = {
        if (symmetric) BreezeUtils.fromBreezeVector(symeval)
        else BreezeUtils.fromBreezeVector(eval)
    }

    /**
      * Return the imaginary parts of the eigenvalues
      */
    def getImagEigenvalues(): DenseVector = {
        if (symmetric) DenseVector.zeros(A.numRows())
        else BreezeUtils.fromBreezeVector(ceval)
    }

    /**
      * Return the block diagonal eigenvalue matrix
      */
    def getD(): DenseMatrix = {
        val n = A.numRows()
        val D = DenseMatrix.zeros(n, n)
        val rev = getRealEigenvalues()
        val iev = getImagEigenvalues()
        for (i <- 0 until n) {
            D.set(i, i, rev.get(i))
            val u = iev.get(i)
            if (u > 0.0)
                D.set(i, i + 1, u)
            else
                D.set(i, i - 1, u)
        }
        D
    }
}


package iit.uvip.psysuite.core.utility

import koma.ndarray.NDArray

/*Measure threshold using a Weibull psychometric function.
Threshold 't' is measured on an abstract 'intensity' scale, which usually corresponds to log10 contrast.

The Weibull psychometric function:

p2=delta*gamma+(1-delta)*(1-(1-gamma)*exp(-10**(beta*(x2+xThreshold))))

where x represents log10 contrast relative to threshold. The Weibull function itself appears only in recompute(), which uses
the specified parameter values in self to compute a psychometric function and store it in self. All the other methods simply use
the psychometric function stored as instance variables. recompute() is called solely by __init__() and beta_analysis() (and possibly by a few user programs). Thus, if
you prefer to use a different kind of psychometric function, called Foo, you need only subclass QuestObject, overriding __init__(), recompute(), and (if you need it) beta_analysis().

instance variables:

tGuess is your prior threshold estimate.

tGuessSd is the standard deviation you assign to that guess.

pThreshold is your threshold criterion expressed as probability of response==1. An intensity offset is introduced into the
psychometric function so that threshold (i.e. the midpoint of the table) yields pThreshold.

beta, delta, and gamma are the parameters of a Weibull psychometric function.

beta controls the steepness of the psychometric function. Typically 3.5.

delta is the fraction of trials on which the observer presses blindly.  Typically 0.01.

gamma is the fraction of trials that will generate response 1 when intensity==-inf.

grain is the quantization of the internal table. E.g. 0.01.

range is the intensity difference between the largest and smallest intensity that the internal table can store. E.g. 5. This interval
will be centered on the initial guess tGuess, i.e. [tGuess-range/2, tGuess+range/2].  QUEST assumes that
intensities outside of this interval have zero prior probability, i.e. they are impossible.*/


class QuestObject() {

    var tGuess:Float    =0F
    var tGuessSd:Float  =0F
    var pThreshold:Float=0F
    var beta:Float      =0F
    var delta:Float     =0F
    var gamma:Float     =0F
    var grain:Float     =0F
    var range:Float     =0F

    lateinit var x:NDArray<Float>
    lateinit var x2:NDArray<Float>
    lateinit var pdf:NDArray<Float>
    lateinit var p2:NDArray<Float>
    lateinit var i:NDArray<Int>
    lateinit var i2:NDArray<Int>


    var dim:Float=0F

    constructor(tGuess:Float=0F,tGuessSd:Float,pThreshold:Float, beta:Float, delta:Float, gamma:Float, grain:Float, range:Float):this(){
        this.tGuess     = tGuess
        this.tGuessSd   = tGuessSd
        this.pThreshold = pThreshold
        this.beta       = beta
        this.delta      = delta
        this.gamma      = gamma
        this.grain      = grain
        this.range      = range
    }

    init{
        /* Create an instance of QuestObject with all the information necessary to measure threshold.
        This was converted from the Psychtoolbox's QuestCreate function.*/
        if(range < 0)               throw Exception("argument \"range\" must be greater than zero")
        else if(range.equals(0F))    dim = 500F
        else                        dim = range/grain
    }




    fun getFirstValue():Float{
        return 50F
    }

    fun getNewValue(res:Boolean):Float{
        return 50F
    }
    /*
    private fun beta_analysis1() {
        //private function called by beta_analysis()
//        if stream is None:
//            stream=sys.stdout
        var q2:MutableList<QuestObject> = mutableListOf()

        for (i in 1 until 17) {// i in range(1,17):
            var q_copy:QuestObject = this.clone(2F.pow(i / 4.0F), 250F, 0.02F)
            q_copy.recompute()
            q2.add(q_copy)
        }
/*
        val na = num.array // shorthand
        t2 = na([q2i.mean() for q2i in q2])
        p2 = na([q2i.pdf_at(t2i) for q2i, t2i in zip(q2, t2)])
        sd2 = na([q2i.sd() for q2i in q2])
        beta2 = na([q2i.beta for q2i in q2])
        i = num.argsort(p2)[-1]
        t = t2[i]
        sd = q2[i].sd()
        p = num.sum(p2)
        betaMean = num.sum(p2 * beta2) / p
        betaSd = math.sqrt(num.sum(p2 * beta2 * *2) / p - (num.sum(p2 * beta2) / p) * *2)
        iBetaMean = num.sum(p2 / beta2) / p
        iBetaSd = math.sqrt(num.sum(p2 / beta2 * *2) / p - (num.sum(p2 / beta2) / p) * *2)
        stream.write(
            '%5.2f	%5.2f	%4.1f	%4.1f	%6.3f\n' % (t,
            sd,
            1 / iBetaMean,
            betaSd,
            self.gamma
        ))
        print 'Now re-analyzing with beta as a free parameter. . . .'

//        if stream is None:
//            stream=sys.stdout
//            stream.write('logC 	 sd 	 beta	 sd	 gamma\n');

        beta_analysis1(stream)

 */
    }

    fun mean():Double {

        //Mean of Quest posterior pdf. Get the mean threshold estimate. This was converted from the Psychtoolbox's QuestMean function.
        return tGuess + sum(pdf*x) / sum(pdf)
    }


    private fun getinf(x) {
        return num.nonzero(isinf(atleast_1d(x)))
    }

    private fun recompute() {

        /*
        //"""Recompute the psychometric function & pdf.

        Call this immediately after changing a parameter of the
        psychometric function. recompute() uses the specified
        parameters in 'self' to recompute the psychometric
        function. It then uses the newly computed psychometric
        function and the history in self.intensity and self.response
        to recompute the pdf. (recompute() does nothing if q.updatePdf
        is False.)

        This was converted from the Psychtoolbox's QuestRecompute function."""
        if not self.updatePdf:
        return
*/
        if (gamma > pThreshold){
            //warnings.warn( 'reducing gamma from %.2f to 0.5'%self.gamma)
            gamma = 0.5F
        }
        i = arange((-dim/2).toDouble(), (dim/2+1).toDouble(), 1.toDouble()) as NDArray<Int>

        x = i.map { it * grain }


        pdf = exp((x/tGuessSd).pow(2).map(it * -0.5) as NDArray<Float> )
        pdf /= sum(pdf) as Float
        i2 = arange(dim.toDouble(), (dim+1).toDouble(), 1.toDouble()) as NDArray<Int>

        x2 = i2.map { it * grain }

        p2 = delta*gamma+(1-delta)*(1-(1-gamma)*exp(-10.pow(x2.map { it * beta})))

        if (p2[0] >= pThreshold || p2[-1] <= pThreshold)
            throw Exception('psychometric function range [%.2f %.2f] omits %.2f threshold'%(p2[0],p2[-1],pThreshold)) //# XXX

        if (len(getinf(p2)[0]))
            throw Exception("psychometric function p2 is not finite")

        val index = num.nonzero( p2[1:] - p2[:-1] )[0]  // strictly monotonic subset

        if (len(index) < 2)
            throw Exception("psychometric function has only %g strictly monotonic points"%len(index))


        val xThreshold = num.interp([pThreshold],p2[index],x2[index])[0]
        p2 = delta*gamma+(1-delta)*(1-(1-gamma)*exp(-10.pow((beta*(x2+xThreshold)))))

        if (len(getinf(p2)[0]))
            throw Exception("psychometric function p2 is not finite")

        s2 = num.array( ((1-p2)[::-1], p2[::-1]) )

        if(not hasattr(self,'intensity') or not hasattr(self,'response'))
            intensity = []
            response = []

        if(len(getinf(self.s2)[0]))
            throw Exception("psychometric function s2 is not finite")

        val eps = 1e-14

        val pL = p2[0]
        val pH = p2[-1]
        var pE = pH*math.log(pH+eps)-pL*math.log(pL+eps)+(1-pH+eps)*math.log(1-pH+eps)-(1-pL+eps)*math.log(1-pL+eps)
        pE = 1/(1+math.exp(pE/(pL-pH)))
        self.quantileOrder=(pE-pL)/(pH-pL)

        if(len(getinf(self.pdf)[0]))
            throw Exception("prior pdf is not finite")

        // recompute the pdf from the historical record of trials
        for(intensity, response in zip(intensity,response)){
            inten = max(-1e10, min(1e10, intensity)) # make intensity finite
            ii = len(pdf) + i - round((inten - tGuess) / grain) - 1
            if ii[0] < 0:
            ii = ii - ii[0]
            if ii[-1] >= s2.shape[1]:
            ii = ii + s2.shape[1] - ii[-1] - 1
            iii = ii.astype(num.int_)
            if not num . allclose (ii, iii):
            raise ValueError ('truncation error')
            pdf = pdf * s2[response, iii]
            if normalizePdf and k % 100 == 0:
            pdf = pdf / num.sum(pdf) # avoid underflow; keep the pdf normalized
        }
        if(normalizePdf)
            pdf = pdf/num.sum(pdf) # avoid underflow; keep the pdf normalized

        if(len(getinf(pdf)[0]))
            throw Exception("prior pdf is not finite")

    }
}
 */

}

fun QuestObject.clone(beta:Float, dim:Float, grain:Float): QuestObject {

    val outqo           =
        QuestObject()
    outqo.tGuess        = this.tGuess
    outqo.tGuessSd      = this.tGuessSd
    outqo.pThreshold    = this.pThreshold
    outqo.delta         = this.delta
    outqo.gamma         = this.gamma
    outqo.range         = this.range

    outqo.beta          = beta
    outqo.dim           = dim
    outqo.grain         = grain

    return outqo
}

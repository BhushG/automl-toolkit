package com.databricks.spark.automatedml.model

import com.databricks.spark.automatedml.executor.DataPrep
import com.databricks.spark.automatedml.params.TreesModelsWithResults
import com.databricks.spark.automatedml.{AbstractUnitSpec, AutomationUnitTestsUtil}

class DecisionTreeTunerTest extends AbstractUnitSpec {

  "DecisionTreeTuner" should "throw UnsupportedOperationException for passing invalid params" in {
    a [UnsupportedOperationException] should be thrownBy {
      new DecisionTreeTuner(null, null).evolveBest()
    }
  }

  it should "should throw UnsupportedOperationException for passing invalid modelSelection" in {
    a [UnsupportedOperationException] should be thrownBy {
      new DecisionTreeTuner(AutomationUnitTestsUtil.getAdultDf(), "err").evolveBest()
    }
  }

  it should "should return valid DecisionTreeTuner" in {
    val adultDataset = AutomationUnitTestsUtil.getAdultDf()
    val treesModelsWithResults: TreesModelsWithResults =  new DecisionTreeTuner(
       new DataPrep(adultDataset).prepData().data,
       "regressor")
      .setFirstGenerationGenePool(5)
      .evolveBest()
    assert(treesModelsWithResults != null, "treesModelsWithResults should not have been null")
    assert(treesModelsWithResults.evalMetrics != null, "evalMetrics should not have been null")
    assert(treesModelsWithResults.model != null, "model should not have been null")
    assert(treesModelsWithResults.modelHyperParams != null, "modelHyperParams should not have been null")
  }

}

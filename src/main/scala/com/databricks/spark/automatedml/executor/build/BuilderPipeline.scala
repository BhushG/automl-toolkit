package com.databricks.spark.automatedml.executor.build

import com.databricks.spark.automatedml.AutomationRunner
import com.databricks.spark.automatedml.executor.DataPrep
import com.databricks.spark.automatedml.sanitize.DataSanitizer
import org.apache.spark.sql.DataFrame

class BuilderPipeline(df: DataFrame) extends BuilderConfig {

  var _modelsToTest: List[String] = List[String]()
  var _modelType: String = ""

  var _labelCol: String = "label"
  var _featuresCol: String = "features"

  var _modelTypeDetectionPrecisionThreshold: Double = 0.01

  var _globalParallelism: Int = 20
  var _localParallelism: Int = 5

  def setModelType(value: String): this.type = {

    require(allowableModelTypes.contains(value), s"$value is not a valid model type.  " +
      s"Please submit one of: ${allowableModelTypes.mkString(", ")}")
    _modelType = value
    this
  }

  def setLabelCol(value: String): this.type = {
    validateLabelColumn(value)
    _labelCol = value
    this
  }

  def setFeaturesCol(value: String): this.type = {
    _featuresCol = value
    this
  }

  def setModelsToTest(value: List[String]): this.type = {

    val checkRegressors = validateModelRestrictions(value, regressorModels)
    val checkClassifiers = validateModelRestrictions(value, classifierModels)

    require(checkClassifiers | checkRegressors, s"Invalid supplied model detected! Please ensure that modelsToTest " +
      s"submission is contained in either: \n\t Regressors: \t\t ${regressorModels.mkString(", ")}" +
      s"\n\t Classifiers: \t\t ${classifierModels.mkString(", ")}")

    _modelsToTest = value
    this
  }

  def setGlobalParallelism(value: Int): this.type = {
    _globalParallelism = value
    this
  }


  def getLabelCol: String = _labelCol
  def getFeaturesCol: String = _featuresCol
  def getModelsToTest: List[String] = _modelsToTest
  def getGlobalParallelism: Int = _globalParallelism








  protected[build] def assignLocalParallelism(): this.type = {
    _localParallelism = math.ceil(_globalParallelism / _modelsToTest.length).toInt
    this
  }

  // Run this first
  protected[build] def assertModelsForEvaluation(): Unit= {

    if(_modelsToTest.isEmpty) {
      validateModelType()
      _modelType match {
        case "regressor" => setModelsToTest(regressorModels)
        case _ => setModelsToTest(classifierModels)
      }
    }
  }


  protected[build] def validateLabelColumn(label: String): Unit = {

    val dfSchema = df.schema.fieldNames
    require(dfSchema.contains(label), s"Supplied label, $label, is not contained in schema of supplied DataFrame!")
  }


  protected[build] def validateModelType(): this.type = {

    val detectedModelType = new DataSanitizer(df)
      .setLabelCol(_labelCol)
      .setFilterPrecision(_modelTypeDetectionPrecisionThreshold)
      .decideModel()

    if(_modelType != "") setModelType(detectedModelType)
    else {
      if (_modelType != detectedModelType) {
        println(s"WARNING! Detected model type: $detectedModelType does not match supplied model type from " +
          s"configuration: ${_modelType} \n \t\t Ensure that model selection type is intentional.")
      }
    }
  this
  }

  protected[build] def validateModelRestrictions(definedModelList: List[String], allowedModelList: List[String]):
  Boolean = {

    val validationChecks = definedModelList.map(x => allowedModelList.contains(x))

    !validationChecks.contains(false)

  }




  // TODO: super-cheese way of doing this is to turn mlflow logging off, run a bunch of automation runner runs
  // and then collect the results at the end to write in one large commit to mlflow.  Will need separate model paths
  // for writing to object store, but won't need any other config changes other than a change to 'best model logging'


//
//  protected[build] def executeRandomForestRegression() = {
//
//    val dataPayload = new DataPrep(df)
//      .setLabelCol(_labelCol)
//      .setFeaturesCol(_featuresCol)
//
//
//
//  }

  //1. Construct the DataPrep config for each model family type
  //2. Construct the Model config
  //3. Instantiate a new execute the full run (without logging) for a family
  //2. Build the configs for each model type
  //3.




//
//  // For each model type, run data prep independently.  THEN, run the model and collect the results.
//  // At the end, write it all out to MLFlow in a logical manner.
//  // Keep a "best of" setting available to log the inference config to reproduce that model in that family.
//  //
//  // EASIER WAY
//  // OR, just build new interfaces to AutomationRunner for each type of model specified (new AutomationRunner(cachedDF))
//  // , make some of those methods protected, and just handle the logging differently (in one main write)
//  // just need to configure the data prep payloads for slightly different types of models (one hot encoding, for instance)
//
}
//
//
//case class CommonConfig(
//                       labelCol: String,
//                       featuresCol: String,
//
//                       //logging config
//
//
//                       //modeling config
//                       scoringMetric: String,
//                       scoringOptimizationStrategy: String,
//                       trainPortion: Double,
//                       trainSplitMethod: String,
//                       trainSplitChronologicalColumn: String,
//                       trainSplitChronologicalRandomPercentage: Double,
//                       parallelism: Int,
//                       kFold: Int,
//                       seed: Long,
//                       dataReductionFactor: Double,
//
//                       //genetic config
//                       firstGenerationGenePool: Int,
//                       numberOfMutationGenerations: Int,
//                       numberOfMutationsPerGeneration: Int,
//                       numberOfParentsToRetain: Int,
//                       geneticMixing: Double,
//                       generationalMutationStrategy: String,
//                       mutationMagnitudeMode: String,
//                       fixedMutationValue: Double,
//                       earlyStoppingFlag: Boolean,
//                       earlyStoppingScore: Double,
//                       evolutionStrategy: String,
//                       continuousEvolutionMaxIterations: Int,
//                       continuousEvolutionStoppingScore: Double,
//                       continuousEvolutionParallelism: Int,
//                       continuousEvolutionMutationAggressiveness: Double,
//                       continuousEvolutionGeneticMixing: String,
//                       continuousEvolutionRollingImprovementCount: Int,
//                       firstGenerationMode: String,
//                       firstGenerationPermutations: Int,
//                       firstGenerationIndexMixingMode: String,
//                       firstGenerationArraySeed: Long,
//
//                       // individual model type thresholds / settings
//                       randomForestRegressorSettings: RandomForestRegressorConfig,
//                       randomForestClassifierSettings: RandomForestClassifierConfig
//
//
//                       // hyper space tuning settings
//
//
//                       )
//
//
//case class GeneralInterfaceConfig(
//                                   labelCol: String,
//                                   featuresCol: String,
//                                   generalDataPrepConfig: GeneralDataPrepConfig,
//
//                                  globalParallelism: Int,
//
//
//                            )
//
//
//case class GeneralDataPrepConfig(
//                                  varianceFiltering: Boolean,
//                                  outlierFiltering: Boolean,
//                                  pearsonFiltering: Boolean,
//                                  covarianceFiltering: Boolean
//                                )
//
//case class RandomForestRegressorConfig(
//)
//
//case class RandomForestClassifierConfig(
//)
//
//
//
//
//
//




import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.PngWriter
import java.nio.file.Files
import scala.io.Source
import java.io.File
import scopt.OParser
import scopt.OParser.builder

def A4Height = 2480
def A4Widht = 3508
def numberOfBillPerPage = 4
def whiteColor = RGBColor.apply(255,255,255).toAWT()
def blank = ImmutableImage.create(A4Height, A4Widht)

@main def main(arg: String*): Unit =
  val config: Config = readUserInput(arg.toArray)
  val folder = config.folder
  val files = loadImagesFromDir(folder).map(x => {
    val filesChunck = splitFilesByNum(numberOfBillPerPage, x)
    // Processing each chunk of images
    filesChunck.zipWithIndex.foreach((chunk, i) => {
      val resImage = imageProcessing(chunk, numberOfBillPerPage)
      saveResult(folder, resImage, s"result_$i.png")
    })
  })

def imageProcessing(files: Array[File], pageSize: Int): ImmutableImage =
  val images = files.map(x => loadImage(x.toString()).scaleToWidth(A4Widht / pageSize)).zipWithIndex
  // TODO NowimageSize must be equal for each pictures. Change it to get pad for each image in future
  val imageSize = images.head._1.width
  val imagesWithPad = images.map((x, i) => (x.padLeft(x.width * i), i))
  // Prepare markup for each of range we have image which cover this pixels
  val imageMarkup: Map[(Range, Range), ImmutableImage] = imagesWithPad.map((x, i) => {
    (((imageSize * i) until (imageSize * (i + 1)), 0 until x.height), x)
  }).toMap

  // for each of blank pixel get image pixel from markup images. if pixel not exist set color as white 
  val resImage = blank.map(pixel => {
    imageMarkup.foldLeft(whiteColor){
      case (acc, ((width, height), image)) => 
        if height.contains(pixel.x) && width.contains(pixel.y) then
          RGBColor.fromRGB(image.rgb(pixel.y, pixel.x)).toAWT()
        else
          acc
    }
  })

  //Mirroring image
  resImage.flipX()

  
def splitFilesByNum(splitNum: Int, files: Array[File]): Array[Array[File]] = 
  files.grouped(splitNum).toArray

def imagesExtension = List("png", "PNG", "jpg", "JPG", "jpeg", "JPEG")

def checkImageContainExtension(imageExtension: List[String], fileName: String): Boolean = 
  imagesExtension.foldLeft(false)((acc, ext) => acc || fileName.contains(ext))

def loadImagesFromDir(dir: String):Option[Array[File]] = 
  val d = File(dir)
  if d.isDirectory() then
    Some(d.listFiles().filter(x => x.isFile() && checkImageContainExtension(imagesExtension, x.toString)))
  else
    None
  
def saveResult(folder: String, image: ImmutableImage, imageName: String) =
  val saveDir = s"$folder/bill"
  File(saveDir).mkdir()
  image.output(PngWriter.MinCompression, s"$saveDir/$imageName")
  
def loadImage(path: String) = ImmutableImageLoader.create().fromFile(path)


case class Config(
  folder: String = ""
)

def parser = 
    OParser.sequence(
      builder.programName("reimbursement"),
      builder.head("reimbursement", "0.0.1"),
      builder.opt[String]('f', "folder")
        .action((x, c: Config) => c.copy(folder = x))
        .text("Please write a proper path to folder with reimbursement images")
        .required()
    )
  

def readUserInput(args: Array[String]): Config = 
  val builder = OParser.builder[Config]
  OParser.parse(parser, args, Config()) match {
    case Some(config) =>
      config
    case _ =>
       System.exit(0)
       Config()
    }
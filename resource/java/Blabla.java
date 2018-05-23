
public class HelloWorld
{
    public static void main(String[] args)
    {
        long start = System.currentTimeMillis();
        crfModel = CRFModel.loadTxt(modelPath, new CRFSegmentModel(new BinTrie<FeatureFunction>()));
        GlobalObjectPool.put(modelPath, crfModel);
    }
}

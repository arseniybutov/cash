package ru.crystals.pos.barcodeprocessing.searchinvalidators;

import ru.crystals.bundles.BundleId;
import ru.crystals.bundles.BundleRef;
import ru.crystals.bundles.ContextBundle;
import ru.crystals.pos.barcodeprocessing.ProductFinder;
import ru.crystals.pos.barcodeprocessing.processors.BarcodeProcessorPlugin;
import ru.crystals.pos.barcodeprocessing.processors.result.BarcodeProcessResult;
import ru.crystals.pos.barcodeprocessing.searchinvalidators.config.SearchInValidatorsProcessorConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.egais.EGAISUtils;
import ru.crystals.pos.egais.EgaisException;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ds.SearchResult;

import java.util.Optional;

/**
 * Плагин поиска алкогольных товаров в сервисах валидации.
 * Позволяет найти продукт по АМ.
 */
@ContextBundle(id = {@BundleId(implemented = BarcodeProcessorPlugin.class, name = "SearchInValidators")}, type = "all", lazy = false)
public class SearchInValidatorsProcessor implements BarcodeProcessorPlugin<SearchInValidatorsProcessorConfig> {

    private static final String PLUGIN_NAME = "SearchInValidators";

    @BundleRef
    private ProductFinder productFinder;

    @BundleRef
    private ExciseValidation exciseValidation;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public Class<SearchInValidatorsProcessorConfig> getConfigClass() {
        return SearchInValidatorsProcessorConfig.class;
    }

    @Override
    public void configure(SearchInValidatorsProcessorConfig config) {
        //do nothing
    }

    @Override
    public boolean supportsBarcode(String excise) {
        try {
            EGAISUtils.checkExciseBarcode(excise);
            return true;
        } catch (EgaisException e) {
            return false;
        }
    }

    @Override
    public BarcodeProcessResult processBarcode(String excise, InsertType insertType) {
        Optional<SearchResult> searchResult = exciseValidation.searchInfoByExcise(excise);
        ProductEntity product = null;

        if (searchResult.isPresent()) {
            SearchResult result = searchResult.get();

            product = tryToSearchProduct(result, insertType);

            //Если это алкогольный товар и не промонабор, выставим excise и вернем
            //В ином случае возвращаем null
            if (product != null
                    && product.getDiscriminator().equals(ProductDiscriminators.PRODUCT_SPIRITS_ENTITY)
                    && !((ProductSpiritsEntity) product).isKit()) {
               product.getProductPositionData().setExcise(excise);
            } else {
                product = null;
            }
        }
        return product == null
                ? BarcodeProcessResult.createNoProductBarcodeProcessResult()
                : BarcodeProcessResult.createOkBarcodeProcessResult(product);
    }

    private ProductEntity tryToSearchProduct(SearchResult result, InsertType insertType) {
        ProductEntity product = productFinder.findProduct(result.getBarcode(), insertType);

        if (product == null) {
            product = productFinder.findProduct(result.getItem(), insertType);
        }

        return product;
    }
}

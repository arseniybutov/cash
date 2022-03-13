package ru.crystals.pos.visualization.products.ciggy;

import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.ProductCiggyPriceEntity;

import javax.swing.table.AbstractTableModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * nbogdanov 
 */
public class PriceTableModel extends AbstractTableModel {

    private ProductCiggyEntity product;
    private List<ProductCiggyPriceEntity> prices = new ArrayList<>();
    private Comparator<ProductCiggyPriceEntity> priceComparator = getComparator() ; 
    
    private static Comparator<ProductCiggyPriceEntity> getComparator(){
        //обратная сортировка для списка цен
        return (o1, o2) -> o2.getPrice().intValue() - o1.getPrice().intValue();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }
    
    @Override
    public String getColumnName(int column) {
        if(column == 0){
            return ResBundleGoodsCiggy.getString("MRC") ;
        }else if(column == 1){
            return ResBundleGoodsCiggy.getString("SALE_PRICE") ;
        }
        return "";
    }

    @Override
    public int getRowCount() {
        if(product == null){
            return 0 ;
        }

        if(product.getAdditionalPrices() == null){
            return 1 ;
        }
        
        return product.getAdditionalPrices().size() ;
    }

    @Override
    public BigDecimal getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return getValueAtFirstColumn(rowIndex) ;
        } else {
            return getValueAtSecondColumn(rowIndex) ;
        }
    }
    
    private BigDecimal getValueAtFirstColumn(int rowIndex){
        if (product != null && product.getAdditionalPrices() != null) {
            return getPriceBD(rowIndex);
        }
        return null;
    }
    
    private BigDecimal getValueAtSecondColumn(int rowIndex){
        if(prices.size() == 0){
            return product.getPrice().getPriceBigDecimal() ;
        }
        if (product != null && product.getAdditionalPrices() != null) {
            return getSalePriceLong(rowIndex) == 0L ? getPriceBD(rowIndex) : getSalePriceBD(rowIndex);
        }
        return null;
    }
    
    private Long getSalePriceLong(int index){
        if(prices.size() == 0 && index == 0){
            return product.getPrice().getPrice() ;
        }
        return  prices.get(index).getSalePriceBigDecimal().longValue() ;
    }
    
    private BigDecimal getPriceBD(int index){
        return prices.get(index).getPriceBigDecimal() ;
    }
    
    private BigDecimal getSalePriceBD(int index){
        return prices.get(index).getSalePriceBigDecimal() ;
    }

    public void setData(ProductCiggyEntity product) {
        prices.clear();
        if (product.getAdditionalPrices() != null) {
            prices.addAll(product.getAdditionalPrices());
            Collections.sort(prices, priceComparator);
        }
        this.product = product;
        fireTableDataChanged();
    }
}

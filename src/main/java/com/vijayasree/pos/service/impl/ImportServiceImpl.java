package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.response.ImportResponse;
import com.vijayasree.pos.entity.Category;
import com.vijayasree.pos.entity.Product;
import com.vijayasree.pos.repository.CategoryRepository;
import com.vijayasree.pos.repository.ProductRepository;
import com.vijayasree.pos.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportServiceImpl implements ImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ImportResponse importProducts(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                totalRows++;
                try {
                    processRow(row);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Failed to import row {}: {}", i + 1, e.getMessage());
                }
            }

        } catch (Exception e) {
            errors.add("Failed to read file: " + e.getMessage());
            log.error("Excel import failed", e);
        }

        return ImportResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failedCount(totalRows - successCount)
                .errors(errors)
                .build();
    }

    private void processRow(Row row) {
        String name = getCellString(row, 0);
        String sku = getCellString(row, 1);
        String categoryName = getCellString(row, 2);
        String priceStr = getCellString(row, 3);
        String stockStr = getCellString(row, 4);
        String lowStockStr = getCellString(row, 5);
        String notes = getCellString(row, 6);
        String imageUrl = getCellString(row, 7);
        String company = getCellString(row, 8);   // ← column I

        if (name.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (sku.isBlank()) throw new IllegalArgumentException("SKU is required");
        if (categoryName.isBlank()) throw new IllegalArgumentException("Category is required");
        if (priceStr.isBlank()) throw new IllegalArgumentException("Price is required");
        if (stockStr.isBlank()) throw new IllegalArgumentException("Stock is required");

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new IllegalArgumentException(
                    "SKU already exists: " + sku + " — product: " + name
            );
        }

        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name(categoryName).build()
                ));

        BigDecimal price;
        int stock;
        int lowStock;

        try { price = new BigDecimal(priceStr); }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid price '" + priceStr + "' for product: " + name
            );
        }

        try { stock = Integer.parseInt(stockStr); }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid stock '" + stockStr + "' for product: " + name
            );
        }

        try { lowStock = lowStockStr.isBlank() ? 5 : Integer.parseInt(lowStockStr); }
        catch (Exception e) { lowStock = 5; }

        Product product = Product.builder()
                .name(name.trim())
                .company(company.isBlank() ? null : company.trim())
                .sku(sku.trim().toUpperCase())
                .category(category)
                .price(price)
                .stockQty(stock)
                .lowStockThreshold(lowStock)
                .notes(notes.isBlank() ? null : notes.trim())
                .active(true)
                .build();

        productRepository.save(product);
        log.info("Imported product: {} ({})", name, sku);
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 9; c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !cell.toString().isBlank()) return false;
        }
        return true;
    }
}
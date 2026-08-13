package com.hutnyk.carfix.file;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class File {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String storageBucket;
    private final String storageKey;
    private final String contentType;
    private final Long bytes;
    private final Integer width;
    private final Integer height;
    private final String sha256Hex;
    private final String altText;
    private final String variants;
    private final FileStatus status;

    //Nullable
    private final OffsetDateTime createdAt;

    //Nullable
    private final OffsetDateTime deletedAt;

    @Builder
    private File(
            Integer id,
            String storageBucket,
            String storageKey,
            String contentType,
            Long bytes,
            Integer width,
            Integer height,
            String sha256Hex,
            String altText,
            String variants,
            FileStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime deletedAt) {
        this.id = id;
        this.storageBucket = Validator.notBlank(storageBucket, "storageBucket");
        this.storageKey = Validator.notBlank(storageKey, "storageKey");
        this.contentType = Validator.notBlank(contentType, "contentType");
        this.bytes = Validator.notNull(bytes, "bytes");
        this.width = Validator.notNull(width, "width");
        this.height = Validator.notNull(height, "height");
        this.sha256Hex = Validator.notBlank(sha256Hex, "sha256Hex");
        this.altText = Validator.notBlank(altText, "altText");
        this.variants = Validator.notBlank(variants, "variants");
        this.status = Validator.notNull(status, "status");
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static File of(
            Integer id,
            String storageBucket,
            String storageKey,
            String contentType,
            Long bytes,
            Integer width,
            Integer height,
            String sha256Hex,
            String altText,
            String variants,
            FileStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime deletedAt) {
        return File.builder()
                .id(id)
                .storageBucket(storageBucket)
                .storageKey(storageKey)
                .contentType(contentType)
                .bytes(bytes)
                .width(width)
                .height(height)
                .sha256Hex(sha256Hex)
                .altText(altText)
                .variants(variants)
                .status(status)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .build();
    }
}

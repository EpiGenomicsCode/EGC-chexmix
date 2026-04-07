package org.egc.core.data.motifdb;

/**
 * Lightweight metadata for background models.
 * Simplified from the original database-backed version to retain only
 * the fields that are actively used in local-only mode.
 */
public class BackgroundModelMetadata {

  protected String name;
  protected int maxKmerLen;
  protected String modelType;
  protected boolean hasCounts;

  public BackgroundModelMetadata(String name, int maxKmerLen, String modelType, boolean hasCounts) {
    this.name = name;
    this.maxKmerLen = maxKmerLen;
    this.modelType = modelType;
    this.hasCounts = hasCounts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMaxKmerLen() {
    return maxKmerLen;
  }

  public String getModelType() {
    return modelType;
  }

  public boolean hasCounts() {
    return hasCounts;
  }

  /**
   * Overridden by BackgroundModel to throw UnsupportedOperationException.
   */
  public void setHasCounts(boolean hasCounts) {
    this.hasCounts = hasCounts;
  }

  public String toString() {
    return name + "\t" + maxKmerLen + "\t" + modelType + "\t" + hasCounts;
  }
}

package com.opt.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pixel-wars")
public class PixelWarsProperties {

    private int width = 120;
    private int height = 80;
    private int cooldownSeconds = 8;
    private String redColor = "#E50000";
    private String blueColor = "#0083C7";
    private int placementPoints = 1;
    private int lineComboLength = 5;
    private int lineComboBonus = 10;
    private int squareComboSize = 3;
    private int squareComboBonus = 15;
    private int captureBonusPerPixel = 3;
    private int bonusZoneIntervalSeconds = 60;
    private int bonusZoneDurationSeconds = 25;
    private int bonusZoneMultiplier = 2;
    private int teamPoolThreshold = 150;
    private int teamPoolBuffDurationSeconds = 30;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getRedColor() {
        return redColor;
    }

    public void setRedColor(String redColor) {
        this.redColor = redColor;
    }

    public String getBlueColor() {
        return blueColor;
    }

    public void setBlueColor(String blueColor) {
        this.blueColor = blueColor;
    }

    public int getPlacementPoints() {
        return placementPoints;
    }

    public void setPlacementPoints(int placementPoints) {
        this.placementPoints = placementPoints;
    }

    public int getLineComboLength() {
        return lineComboLength;
    }

    public void setLineComboLength(int lineComboLength) {
        this.lineComboLength = lineComboLength;
    }

    public int getLineComboBonus() {
        return lineComboBonus;
    }

    public void setLineComboBonus(int lineComboBonus) {
        this.lineComboBonus = lineComboBonus;
    }

    public int getSquareComboSize() {
        return squareComboSize;
    }

    public void setSquareComboSize(int squareComboSize) {
        this.squareComboSize = squareComboSize;
    }

    public int getSquareComboBonus() {
        return squareComboBonus;
    }

    public void setSquareComboBonus(int squareComboBonus) {
        this.squareComboBonus = squareComboBonus;
    }

    public int getCaptureBonusPerPixel() {
        return captureBonusPerPixel;
    }

    public void setCaptureBonusPerPixel(int captureBonusPerPixel) {
        this.captureBonusPerPixel = captureBonusPerPixel;
    }

    public int getBonusZoneIntervalSeconds() {
        return bonusZoneIntervalSeconds;
    }

    public void setBonusZoneIntervalSeconds(int bonusZoneIntervalSeconds) {
        this.bonusZoneIntervalSeconds = bonusZoneIntervalSeconds;
    }

    public int getBonusZoneDurationSeconds() {
        return bonusZoneDurationSeconds;
    }

    public void setBonusZoneDurationSeconds(int bonusZoneDurationSeconds) {
        this.bonusZoneDurationSeconds = bonusZoneDurationSeconds;
    }

    public int getBonusZoneMultiplier() {
        return bonusZoneMultiplier;
    }

    public void setBonusZoneMultiplier(int bonusZoneMultiplier) {
        this.bonusZoneMultiplier = bonusZoneMultiplier;
    }

    public int getTeamPoolThreshold() {
        return teamPoolThreshold;
    }

    public void setTeamPoolThreshold(int teamPoolThreshold) {
        this.teamPoolThreshold = teamPoolThreshold;
    }

    public int getTeamPoolBuffDurationSeconds() {
        return teamPoolBuffDurationSeconds;
    }

    public void setTeamPoolBuffDurationSeconds(int teamPoolBuffDurationSeconds) {
        this.teamPoolBuffDurationSeconds = teamPoolBuffDurationSeconds;
    }
}

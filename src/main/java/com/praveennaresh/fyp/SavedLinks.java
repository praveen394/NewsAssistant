package com.praveennaresh.fyp;

/**
 * Praveen Naresh
 * Created on 20-Jan-16
 * Saved Links model
 */
public class SavedLinks {
    private int id;
    private String title;
    private String link;

    public SavedLinks(int id, String title, String link)
    {
        this.id = id;
        this.title = title;
        this.link = link;
    }

    public int getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLink()
    {
        return link;
    }

    @Override
    public String toString()
    {
        return title;
    }
}

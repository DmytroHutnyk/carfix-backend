package com.hutnyk.carfix;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PasswordsReader {
    public static Set<String> readPasswords(String fileLocation){

        Set<String> result = new HashSet<>();
        try(InputStream inputStream = PasswordsReader.class.getResourceAsStream(fileLocation);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream, "Recourse not found: " + fileLocation)));){

            String line;
            while ((line = bufferedReader.readLine()) != null){
                if(line.startsWith("#")){
                    continue;
                }
                result.add(line);
            }
        }catch (IOException e){
            throw new RuntimeException("Failed to load passwords: " + e.getMessage());
        }
        return result;
    }
}

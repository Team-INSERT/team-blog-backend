package com.project.insert.domain.post.service;

import com.project.insert.domain.post.Image;
import com.project.insert.domain.post.Post;
import com.project.insert.domain.post.entity.repository.ImageRepository;
import com.project.insert.domain.post.entity.repository.PostRepository;
import com.project.insert.presentation.post.dto.ImageFormat;
import com.project.insert.presentation.post.dto.PostDto;
import com.project.insert.presentation.post.dto.PostReadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.plaf.multi.MultiListUI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;

    /*게시글 생성*/
    @Transactional
    public void createPost(PostDto postDto, List<MultipartFile> files, List<String> types) throws IOException{

        Post post = new Post(postDto.getTitle(), postDto.getContent());
        Post save = postRepository.save(post);

        List<MultipartFile> validatedFiles = filesValidation(files);

        filesUpload(validatedFiles, save.getId());

        for(MultipartFile validatedFile : validatedFiles){
            Image image = new Image(validatedFile, save);
            imageRepository.save(image);
        }
    }

    /*게시글 전체 조회 및 썸네일 조회*/
    public List<PostDto> readAll() {
        List<Post> posts = postRepository.findAll();
        List<PostDto> postDtos = new ArrayList<>();


        for (Post post : posts) {
            List<Image> imageList = imageRepository.findAllByPostId(post.getId());

            PostDto postDto = new PostDto(post, imageList.get(0));


            postDtos.add(postDto);
        }

        return postDtos;
    }


    /*게시글 세부 조회(읽기)*/
    public PostReadDto readOne(Long postId){
        Optional<Post> optionalPost = postRepository.findById(postId);

        if(optionalPost.isEmpty()){
            throw new IllegalArgumentException("해당 게시글이 없습니다");
        }

        Post post = optionalPost.get();

        List<Image> imageList = imageRepository.findAllByPostId(postId);
        List<ImageFormat> imageFormatList = new ArrayList<>();

        if (imageList != null){
            for (Image image : imageList){
                ImageFormat imageFormat = new ImageFormat(image);
                imageFormatList.add(imageFormat);
            }
        }

        PostReadDto postReadDto = new PostReadDto(post.getTitle(), post.getContent(), imageFormatList);
        return postReadDto;

    }


    private List<MultipartFile> filesValidation(List<MultipartFile> files) throws IOException{
        String[] accessDeniedFileExtension = {"exe", "zip"};
        String[] accessDeniedFileContentType = {"application/x-msdos-program", "application/zip"};

        ArrayList<MultipartFile> validatedFiles = new ArrayList<>();

        for(MultipartFile file : files){

            String originalImageName = file.getOriginalFilename();

            String fileExtension = originalImageName.substring(originalImageName.lastIndexOf(".") + 1);

            String imageContentType = file.getContentType();

            if (Arrays.asList(accessDeniedFileExtension).contains(fileExtension) ||
                    Arrays.asList(accessDeniedFileContentType).contains(imageContentType)) {
                log.warn(fileExtension + "(" + imageContentType + ") 파일은 지원하지 않는 확장자입니다.");
            } else{
                validatedFiles.add(file);
            }
        }
        return validatedFiles;
    }

    private void filesUpload(List<MultipartFile> files, Long postId) throws IOException{

        String rootDir = System.getProperty("user.dir");

        for(MultipartFile file : files){
            java.io.File uploadPath = new java.io.File(rootDir + "/media/" + postId + "_" + file.getOriginalFilename());
            file.transferTo(uploadPath);
        }
    }
}
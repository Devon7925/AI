class Square extends Rectangle{
    public Square(Point center, double length){
        super(center.retmove(-length/2, -length/2), length, length);
    }
}